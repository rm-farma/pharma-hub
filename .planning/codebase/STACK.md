# Technology Stack

**Analysis Date:** 2026-08-06

## Languages

**Primary:**
- Java 21 - Core application language, configured in `pom.xml` via `maven.compiler.release` property

**Secondary:**
- SQL - Query definitions stored in `src/main/resources/queries/` directory as YAML metadata with SQL files

## Runtime

**Environment:**
- Java 21 Eclipse Temurin JRE (Alpine-based in production Docker image)
- Quarkus 3.27.2 - Lightweight, fast-starting Java framework optimized for cloud deployment

**Package Manager:**
- Maven 3.9 - Build tool configured in `pom.xml`
- Lockfile: Not explicitly present; Maven manages dependency versions via BOM (Bill of Materials)

## Frameworks

**Core:**
- Quarkus 3.27.2 - REST application framework with built-in optimizations for Cloud Run
  - Location: Imported via `quarkus-bom` in `pom.xml`
  - Configuration: `src/main/resources/application.properties`, `application-dev.properties`, `application-prod.properties`

**API & REST:**
- Quarkus REST (Jakarta REST) - RESTful endpoint handling
  - Dependency: `quarkus-rest` and `quarkus-rest-jackson` in `pom.xml`
  - Resources: `src/main/java/com/rmfarma/pharmahub/api/resource/` - REST endpoints for query execution and catalog

**Documentation & API Spec:**
- SmallRye OpenAPI - OpenAPI/Swagger specification generation
  - Dependency: `quarkus-smallrye-openapi` in `pom.xml`
  - Spec endpoint: `/q/openapi` (JSON/YAML)
  - Swagger UI: `/q/swagger-ui` (dev only, disabled in prod via `application-prod.properties`)
  - Configuration: `application-dev.properties` lines 43-62

**Data Access:**
- Quarkus Google Cloud Services — BigQuery (`quarkus-google-cloud-bigquery`, resolved 2.18.0) - injects the `BigQuery` client used to run table-function queries as BigQuery jobs
  - Dependency: `io.quarkiverse.googlecloudservices:quarkus-google-cloud-bigquery` in `pom.xml`
  - No connection pooling concept (unlike JDBC/Agroal, which this replaced) — each query submits a job via `bigquery.query(QueryJobConfiguration)`
  - Data project: `rmfarma`, dataset `ISAZ` — same project as the app's own GCP project in prod (`rmfarma`); cross-project in dev, where the app runs under `rmfarma-dev`

**Testing:**
- JUnit 5 - Test runner
  - Dependency: `quarkus-junit5` (test scope) in `pom.xml` line 91-93
  - Configured via Maven Surefire plugin in `pom.xml` lines 126-135

- REST Assured - HTTP client for testing REST endpoints
  - Dependency: `rest-assured` (test scope) in `pom.xml` line 95-98

**Build & Compilation:**
- Quarkus Maven Plugin - Build plugin for compilation, native-image generation, and code generation
  - Configuration: `pom.xml` lines 103-117
  - Goals: `build`, `generate-code`, `generate-code-tests`, `native-image-agent`

- Maven Compiler Plugin 3.14.0 - Java compilation
  - Configuration: `pom.xml` lines 119-125
  - Parameters enabled for reflection

- Maven Surefire Plugin 3.5.3 - Unit test execution
  - Configuration: `pom.xml` lines 126-135

- Maven Failsafe Plugin 3.5.3 - Integration test execution
  - Configuration: `pom.xml` lines 136-154
  - Native profile: Runs ITs against native image when `-Dnative` flag is used

## Key Dependencies

**Critical:**
- `quarkus-bom:io.quarkus.platform` 3.27.2 - Bill of Materials providing consistent versions for all Quarkus dependencies
- `quarkus-google-cloud-services-bom:io.quarkus.platform` 3.27.2 - Google Cloud integrations (Secret Manager, Logging)

**Infrastructure:**
- `quarkus-google-cloud-bigquery` (Quarkiverse, resolved 2.18.0) - injects the `BigQuery` client used by `BigQueryQueryExecutor` to run table-function queries
- `quarkus-google-cloud-secret-manager` - Integration with GCP Secret Manager for credential injection (dev environment)

**Observability & Logging:**
- `quarkus-micrometer-registry-prometheus` - Prometheus metrics export at `/q/metrics`
  - Configured in `application.properties` line 11
  - Used for monitoring in Cloud Run

- `quarkus-logging-json` - JSON-formatted structured logging for Cloud Logging integration
  - Configured in `application-prod.properties` line 21 to output JSON logs for Stackdriver/Cloud Logging

- `quarkus-smallrye-health` - Health check endpoint at `/q/health`
  - Manual health check implementation in `src/main/java/com/rmfarma/pharmahub/api/resource/HealthResource.java`

**Configuration & YAML:**
- `quarkus-config-yaml` - YAML configuration file support
- `snakeyaml` - YAML parsing library

## Configuration

**Environment:**
- Profile-based configuration: `application.properties` (base), `application-dev.properties` (dev), `application-prod.properties` (prod)
  - Activated via `QUARKUS_PROFILE` environment variable (prod profile in Cloud Run)
  - Dev profile activates automatically with `./mvnw quarkus:dev`

- BigQuery access relies on Application Default Credentials / the Cloud Run service account (no JDBC secrets to inject anymore) — see `.planning/codebase/INTEGRATIONS.md` for the required IAM roles.
- GCP Secret Manager (dev): Credentials fetched automatically via `gcloud auth application-default login`
  - Syntax: `${sm//secret-name}` for Secret Manager reference

**Build:**
- Maven build configuration: `pom.xml`
- Quarkus package type: `uber-jar` (single executable JAR with all dependencies)
  - Configured via `-Dquarkus.package.jar.type=uber-jar` in Cloud Build
  - Output: `target/*-runner.jar`

- Native image support: Optional via Maven profile `-Dnative`
  - Configuration: `pom.xml` lines 158-172
  - Native agent: `quarkus-maven-plugin` goal `native-image-agent`

## Platform Requirements

**Development:**
- Java 21 (Eclipse Temurin or compatible)
- Maven 3.9+
- gcloud CLI (for GCP Secret Manager and BigQuery authentication with `gcloud auth application-default login`)
- BigQuery access to project `rmfarma`, dataset `ISAZ` (cross-project IAM — see INTEGRATIONS.md)

**Production:**
- Deployment target: Google Cloud Run (managed container service)
  - Image registry: Artifact Registry (`southamerica-east1-docker.pkg.dev`)
  - Docker image: Alpine-based (`eclipse-temurin:21-jre-alpine`)
  - Memory allocation: 1Gi (prod), 512Mi (nonprod)
  - CPU allocation: 1 CPU (both prod and nonprod)
  - Concurrency: 80 requests per instance
  - Auto-scaling: 1-10 instances (prod), 0-3 instances (nonprod)
  - Timeout: 60 seconds per request

**Port:** 8080 (configured in `application.properties` line 7)

---

*Stack analysis: 2026-08-06*
