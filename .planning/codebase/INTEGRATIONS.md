# External Integrations

**Analysis Date:** 2026-08-06

## APIs & External Services

**GCP Secret Manager:**
- Service: Google Cloud Secret Manager
  - What it's used for: Secure credential management and injection into application configuration
  - SDK/Client: `quarkus-google-cloud-secret-manager` (Quarkus integration)
  - Auth: gcloud CLI (`gcloud auth application-default login`) for dev environment
  - Reference syntax: `${sm//secret-name}` in properties files (e.g., `${sm//gpt-db-host}` in `application-dev.properties` line 17)
  - Secrets managed (prod):
    - `pharmahub_db_url` - Database connection URL
    - `pharmahub_db_user` - Database username
    - `pharmahub_db_password` - Database password
    - `pharmahub_api_key_pharma_app` - API key for pharma app client
    - `pharmahub_api_key_admin_dashboard` - API key for admin dashboard client
  - Configuration: `application-dev.properties` line 12 sets GCP project ID (`rmfarma-dev`)

**GCP Cloud Logging (Stackdriver):**
- Service: Google Cloud Logging
  - What it's used for: Centralized log aggregation and analysis in Cloud Run
  - SDK/Client: `quarkus-logging-json` - Outputs logs in JSON format compatible with Cloud Logging
  - Configuration: `application-prod.properties` lines 20-22
    - `quarkus.log.console.json=${LOG_JSON:true}` - JSON format enabled in prod
    - `GCP_LOGGING_ENABLED: "true"` environment variable (prod)
  - Structured logging from resources: e.g., `LOG.infov()` in `QueryExecutionResource.java` line 578

## Data Storage

**Databases:**
- **PostgreSQL** (primary)
  - Type: Relational database
  - Connection: JDBC via Agroal connection pool
  - Connection string environment variable:
    - Dev: Resolved from GCP Secret Manager as `${sm//gpt-db-host}`
    - Prod: Injected via Cloud Build as `DATABASE_URL` environment variable
  - Client library: `quarkus-jdbc-postgresql`
  - Host (dev): `104.198.194.196:5432`
  - Database name (dev): `base_de_conhecimento`
  - Connection pool configuration:
    - Dev: min=1, max=5 connections (`application-dev.properties` lines 20-21)
    - Prod: min=2, max=10 connections (`application-prod.properties` lines 14-15)
  - Query definitions: SQL files in `src/main/resources/queries/*/` directory with YAML metadata
  - Mapper classes: `src/main/java/com/rmfarma/pharmahub/infrastructure/mapper/queries/` - Result set mapping for specific queries (e.g., `TopSellerMapper.java`, `SalesSummaryMapper.java`)

**File Storage:**
- Local filesystem only
  - Query definitions stored as YAML files: `src/main/resources/queries/{query-key}/`
  - Each query has `metadata.yaml` defining parameters, pagination, execution modes
  - No cloud storage integration (S3, GCS, etc.)

**Caching:**
- None implemented
  - Queries execute directly against PostgreSQL without caching layer
  - Application is stateless, suitable for auto-scaling in Cloud Run

## Authentication & Identity

**Auth Provider:**
- Custom API Key authentication (no external OAuth/OIDC provider)
  - Implementation: X-API-Key header validation in request interceptors/filters
  - API keys stored in configuration: `queryhub.security.api-keys.*` properties
  - Keys (hardcoded in `application.properties` line 23-24, apply to every profile including prod — não há chaves específicas de prod hoje):
    - `dev-client`: chave compartilhada dev+prod (ver `application.properties`, não hardcoded neste doc)
    - `test-client`: `test-api-key-456`
  - ⚠️ `pharma-app`/`admin-dashboard` (Secret Manager) foram removidos em 2026-08-07 por não terem cliente real associado — ver histórico do git se precisar reintroduzir
  - Configuration class: `src/main/java/com/rmfarma/pharmahub/infrastructure/config/ApiKeyConfig.java`
  - Usage: Checked in resource layer (e.g., `QueryExecutionResource.java` line 542-543 extracts X-Client-Id header)
  - OpenAPI security scheme: `ApiKeyAuth` - defined in `application-dev.properties` lines 58-62

- Endpoint protection:
  - Health check `/health` - **No authentication required** (marked in `HealthResource.java` line 33)
  - All query endpoints - **Authentication required** via X-API-Key header
  - OpenAPI/Swagger endpoints - Disabled in production (`application-prod.properties` lines 35-36)

## Monitoring & Observability

**Error Tracking:**
- None configured (no external error tracking service like Sentry, DataDog, etc.)
- Error responses logged locally and forwarded to Cloud Logging in prod

**Logs:**
- Strategy: Structured JSON logging in production, console logging in development
  - Dev: `quarkus.log.console.json=false` - Human-readable format with timestamp, level, logger name, thread, message (`application-dev.properties` line 27-28)
  - Prod: `quarkus.log.console.json=true` - JSON format for Cloud Logging integration (`application-prod.properties` line 21)
  - Log levels:
    - Base: `INFO` (configured in `application-prod.properties` line 20)
    - Dev category override: `DEBUG` for `com.rmfarma` namespace (`application-dev.properties` line 29)
  - Log manager: JBoss LogManager (configured in Maven Surefire plugin `pom.xml` line 131)

**Metrics:**
- Prometheus metrics export
  - Framework: Micrometer with Prometheus registry
  - Dependency: `quarkus-micrometer-registry-prometheus`
  - Endpoint: `/q/metrics` (configured in `application.properties` line 11)
  - Exposed metrics: Default JVM metrics (GC, memory, threads) + Quarkus application metrics
  - Scrape target: Cloud Monitoring or external Prometheus instance can scrape metrics from this endpoint

**Health Checks:**
- Quarkus SmallRye Health framework
  - Endpoint: `/q/health` (Quarkus management endpoint, separate from `/health` custom endpoint)
  - Custom implementation: `src/main/java/com/rmfarma/pharmahub/api/resource/HealthResource.java`
    - Tests database connectivity with `SELECT 1` query
    - Returns `{"status": "UP", "database": "connected"}` or error response
  - No authentication required (configured in `HealthResource.java` line 33)

## CI/CD & Deployment

**Hosting:**
- Google Cloud Run (managed container service, serverless)
  - Deployment region: `southamerica-east1`
  - Service name: `pharma-hub`
  - Image registry: Artifact Registry in `southamerica-east1-docker.pkg.dev`

**CI Pipeline:**
- GitHub Actions (for push/PR to main branch)
  - Workflow: `.github/workflows/ci.yml`
  - Trigger: Push to `main` or PR to `main`
  - Steps:
    1. Checkout code
    2. Set up Java 21 (Eclipse Temurin)
    3. Build with Maven: `./mvnw verify` (includes tests, compilation, packaging)
  - Maven cache enabled for dependency caching
  - Configuration: `.github/workflows/ci.yml` lines 1-26

- Google Cloud Build (for image building and deployment)
  - Two pipelines:
    - **Production** (`cloudbuild-prod.yaml`): Triggered on `main` branch
      - Builds Maven uber-jar with Maven 3.9 and Java 21
      - Builds Docker image with Alpine JRE base
      - Pushes image to Artifact Registry (tagged with commit SHA and `latest`)
      - Deploys to Cloud Run with production configuration (1Gi memory, 1-10 instances)
      - Sets environment variables: `QUARKUS_PROFILE=prod`, `LOG_LEVEL`, `LOG_JSON=true`, `GCP_LOGGING_ENABLED=true`
      - Injects secrets from Secret Manager for database credentials and API keys
      - Timeout: 60 seconds per request
      - Concurrency: 80 requests per instance
      - No public access (`--no-allow-unauthenticated`)

    - **Non-Production** (`cloudbuild-nonprod.yaml`): Triggered on `dev` branch
      - Same build process as prod but with reduced resources (512Mi memory, 0-3 instances)
      - Sets `LOG_LEVEL: DEBUG` for increased verbosity in staging/dev environment
      - Deploys to same region but different Cloud Run service or configuration

**Secret Management:**
- Google Cloud Secret Manager integration
  - Secrets injected via Cloud Build `--set-secrets` flag (references Secret Manager secret versions)
  - Environment-specific secret names:
    - Prod: `pharmahub_db_url`, `pharmahub_db_user`, `pharmahub_db_password`, `pharmahub_api_key_pharma_app`, `pharmahub_api_key_admin_dashboard`
    - Nonprod: `gpt_db_host`, `gpt_db_user`, `gpt_db_password`
  - No hardcoded secrets in code or Cloud Build YAML

## Environment Configuration

**Required env vars:**

**Development (local):**
- Configured via GCP Secret Manager (automatic injection)
- Local reference file: `env.yaml` (git-ignored, not committed)
  - `DB_URL`: JDBC connection string
  - `DB_NAME`: Database name
  - `DB_USERNAME`: Database username
  - `DB_PASSWORD`: Database password
  - `LOG_LEVEL`: Logging level (DEBUG/INFO/WARN)
  - `LOG_JSON`: JSON logging flag (false for dev, true for prod)
  - `GCP_LOGGING_ENABLED`: Cloud Logging integration flag

**Production (Cloud Run):**
- Injected by Cloud Build via `--set-env-vars` flag:
  - `QUARKUS_PROFILE=prod` - Activates prod configuration profile
  - `LOG_LEVEL=${_LOG_LEVEL}` - Set to `INFO` in prod
  - `LOG_JSON=true` - Enable JSON logging
  - `GCP_LOGGING_ENABLED=true` - Enable Cloud Logging
- Injected as secrets from Secret Manager:
  - `DATABASE_URL` - JDBC connection string
  - `DATABASE_USER` - Database username
  - `DATABASE_PASSWORD` - Database password
  - `API_KEY_PHARMA_APP` - API key for pharma app
  - `API_KEY_ADMIN_DASHBOARD` - API key for admin dashboard

**Secrets location:**
- Google Cloud Secret Manager (GCP project `rmfarma` for prod, `rmfarma-dev` for dev)
- Access model: Cloud Build service account has permission to retrieve secrets during deployment
- Local dev: Automatic via `gcloud auth application-default login` (personal GCP credentials)

## Webhooks & Callbacks

**Incoming:**
- None - Application is a read-only query execution service, no webhooks received

**Outgoing:**
- None - Application does not send webhooks or callbacks to external systems
- Queries execute against PostgreSQL only, no downstream API calls

## API Endpoints

**Public REST API:**
- Base path: `/` (Quarkus REST root)
- Endpoints:
  - `POST /queries/{key}/execute` - Execute a named query with parameters and pagination options
    - Location: `src/main/java/com/rmfarma/pharmahub/api/resource/QueryExecutionResource.java`
    - Authentication: X-API-Key header (required)
    - Response formats: JSON (paged or unpaged based on request)

  - `GET /queries` - List all available queries in the catalog
    - Location: `src/main/java/com/rmfarma/pharmahub/api/resource/QueryCatalogResource.java`
    - Authentication: X-API-Key header (required)

  - `GET /queries/{key}` - Get details of a specific query (parameters, pagination options, examples)
    - Location: `src/main/java/com/rmfarma/pharmahub/api/resource/QueryCatalogResource.java`
    - Authentication: X-API-Key header (required)

  - `GET /health` - Application health check (custom endpoint)
    - Location: `src/main/java/com/rmfarma/pharmahub/api/resource/HealthResource.java`
    - Authentication: **Not required**
    - Tests database connectivity

**Management Endpoints (Quarkus built-in):**
- `/q/health` - Quarkus health check
- `/q/metrics` - Prometheus metrics
- `/q/openapi` - OpenAPI specification (JSON/YAML)
- `/q/swagger-ui` - Swagger UI (dev only, disabled in prod)

---

*Integration audit: 2026-08-06*
