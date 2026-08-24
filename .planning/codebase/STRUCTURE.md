# Codebase Structure

**Analysis Date:** 2026-08-06

## Directory Layout

```
pharma-hub/
├── src/
│   ├── main/
│   │   ├── java/com/rmfarma/pharmahub/
│   │   │   ├── api/                          # REST API layer
│   │   │   │   ├── OpenApiConfig.java        # SmallRye OpenAPI configuration
│   │   │   │   ├── resource/                 # JAX-RS endpoints
│   │   │   │   │   ├── QueryExecutionResource.java
│   │   │   │   │   ├── QueryCatalogResource.java
│   │   │   │   │   └── HealthResource.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   └── ExecuteRequest.java
│   │   │   │   │   └── response/
│   │   │   │   │       ├── PagedResponse.java
│   │   │   │   │       ├── UnpagedResponse.java
│   │   │   │   │       ├── QueryInfoResponse.java
│   │   │   │   │       ├── ErrorResponse.java
│   │   │   │   │       └── queries/           # Query-specific DTOs (15 types)
│   │   │   │   │           ├── SalesSummaryDTO.java
│   │   │   │   │           ├── TopProductDTO.java
│   │   │   │   │           └── ... (13 more)
│   │   │   │   ├── filter/
│   │   │   │   │   └── ApiKeyFilter.java     # X-API-Key authentication
│   │   │   │   └── exception/
│   │   │   │       └── GlobalExceptionMapper.java
│   │   │   │
│   │   │   ├── application/                  # Business logic / Use Cases
│   │   │   │   ├── ExecuteQueryUseCase.java
│   │   │   │   ├── ListQueriesUseCase.java
│   │   │   │   └── GetQueryDetailsUseCase.java
│   │   │   │
│   │   │   ├── core/                         # Domain layer (no externals)
│   │   │   │   ├── model/
│   │   │   │   │   ├── QueryDefinition.java  # Query metadata record
│   │   │   │   │   ├── ParamDefinition.java  # Parameter metadata record
│   │   │   │   │   ├── ParamType.java        # Enum: type system for params
│   │   │   │   │   ├── ExecutionMode.java    # Enum: PAGED or UNPAGED
│   │   │   │   │   ├── PagedResult.java      # Result wrapper with pagination
│   │   │   │   │   ├── UnpagedResult.java    # Result wrapper without pagination
│   │   │   │   │   └── PaginationRequest.java
│   │   │   │   ├── port/
│   │   │   │   │   ├── QueryRepository.java  # Interface: load query definitions
│   │   │   │   │   └── QueryExecutor.java    # Interface: execute SQL
│   │   │   │   └── exception/
│   │   │   │       ├── QueryNotFoundException.java
│   │   │   │       ├── ParamValidationException.java
│   │   │   │       ├── UnpagedNotAllowedException.java
│   │   │   │       └── MaxRowsExceededException.java
│   │   │   │
│   │   │   └── infrastructure/                # Technical implementations
│   │   │       ├── config/
│   │   │       │   ├── ApiKeyConfig.java     # Loads security.api-keys from props
│   │   │       │   └── QueryHubConfig.java   # Loads pagination/timeout settings
│   │   │       ├── query/
│   │   │       │   └── FileSystemQueryRepository.java  # Loads YAML + SQL
│   │   │       ├── bigquery/
│   │   │       │   ├── BigQueryQueryExecutor.java
│   │   │       │   └── BigQueryParamResolver.java
│   │   │       └── mapper/
│   │   │           ├── RowMapper.java        # Interface: FieldValueList → T
│   │   │           ├── BigQueryValues.java   # Null-safe FieldValueList column extraction
│   │   │           ├── GenericMapMapper.java # Default: row → Map (positional keys)
│   │   │           └── queries/              # Query-specific mappers (15)
│   │   │               ├── SalesSummaryMapper.java
│   │   │               ├── TopProductMapper.java
│   │   │               └── ... (13 more)
│   │   │
│   │   ├── resources/
│   │   │   ├── application.properties        # Base config (all profiles)
│   │   │   ├── application-dev.properties    # Dev overrides
│   │   │   ├── application-prod.properties   # Prod overrides (env vars)
│   │   │   └── queries/                      # Query definitions
│   │   │       ├── sales-summary/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── top-sellers/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── top-products/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── stock-search/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── stock-metrics/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── sales-overview/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── sales-comparison/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── idle-stock/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── abc-curve-summary/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── abc-curve-products/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── stock-without-sales/
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── items-sold-below-cost/    # New (BigQuery migration)
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── manufacturer-sales/       # New (BigQuery migration)
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       ├── products-loss/            # New (BigQuery migration)
│   │   │       │   ├── metadata.yaml
│   │   │       │   └── query.sql
│   │   │       └── top-products-by-category/ # New (BigQuery migration)
│   │   │           ├── metadata.yaml
│   │   │           └── query.sql
│   │   │
│   │   └── docker/
│   │       ├── Dockerfile.jvm
│   │       ├── Dockerfile.native
│   │       ├── Dockerfile.native-micro
│   │       └── Dockerfile.legacy-jar
│   │
│   └── test/
│       └── java/com/rmfarma/pharmahub/       # Test classes (currently empty)
│
├── pom.xml                                   # Maven configuration (Quarkus 3.27.2)
├── env.yaml                                  # Local env vars (dev only, not committed)
├── .planning/
│   └── codebase/                             # This analysis
│       ├── ARCHITECTURE.md
│       ├── STRUCTURE.md
│       └── (other docs if generated)
│
└── target/                                   # Maven build output (generated)
```

## Directory Purposes

**`src/main/java/com/rmfarma/pharmahub/api/`**
- Purpose: HTTP request/response handling, endpoint definitions, API documentation
- Contains: JAX-RS resources (@Path, @GET, @POST), DTOs, filters, exception mappers
- Key files: QueryExecutionResource (POST /queries/{key}/execute), QueryCatalogResource (GET /queries*)

**`src/main/java/com/rmfarma/pharmahub/api/resource/`**
- Purpose: JAX-RS REST endpoints
- Contains: Three resource classes — one for query execution, one for catalog, one for health
- Patterns: Each resource is @ApplicationScoped singleton, methods return Response or mapped objects

**`src/main/java/com/rmfarma/pharmahub/api/dto/`**
- Purpose: Request/response objects (serialization/deserialization)
- Contains: ExecuteRequest (input), PagedResponse/UnpagedResponse (output), QueryInfoResponse, ErrorResponse
- Patterns: Records (immutable), generic types for flexible result wrapper

**`src/main/java/com/rmfarma/pharmahub/application/`**
- Purpose: Use cases — business logic orchestration without framework dependencies
- Contains: Three use case classes (Execute, List, Details) implementing specific operations
- Patterns: Each @ApplicationScoped, receives ports via constructor injection, throws domain exceptions

**`src/main/java/com/rmfarma/pharmahub/core/`**
- Purpose: Domain layer (business rules, abstractions, no technical details)
- Contains: Models (records), Ports (interfaces), Exceptions (domain-specific)
- Patterns: Dependency inversion — core defines what infrastructure must implement

**`src/main/java/com/rmfarma/pharmahub/core/model/`**
- Purpose: Domain entities and value objects
- Contains: QueryDefinition, ParamDefinition, ParamType (enum), ExecutionMode (enum), Result wrappers
- Patterns: Records for immutability, enums for strategies (ParamType has convert/toQueryParameterValue methods)

**`src/main/java/com/rmfarma/pharmahub/core/port/`**
- Purpose: Abstractions (interfaces) that drive infrastructure decisions
- Contains: QueryRepository (query metadata source), QueryExecutor (SQL execution)
- Patterns: Port-Adapter pattern — interfaces here, implementations in infrastructure/

**`src/main/java/com/rmfarma/pharmahub/infrastructure/`**
- Purpose: Technical implementations (BigQuery access, I/O, configuration)
- Contains: Repository impl, BigQuery executor, mappers, config loaders
- Patterns: Adapters realizing core ports; @ApplicationScoped singletons

**`src/main/java/com/rmfarma/pharmahub/infrastructure/query/`**
- Purpose: Load and cache query definitions
- Contains: FileSystemQueryRepository — scans classpath for queries/*/metadata.yaml + query.sql
- Patterns: @PostConstruct init loads all queries once; queries cached in memory

**`src/main/java/com/rmfarma/pharmahub/infrastructure/bigquery/`**
- Purpose: BigQuery access — each `query.sql` is now a single table-function call
- Contains: BigQueryQueryExecutor (pagination, job submission), BigQueryParamResolver (`@param` → `QueryParameterValue` bindings)
- Patterns: Injected `BigQuery` client (Quarkiverse extension), no connection pooling — each call is a submitted job

**`src/main/java/com/rmfarma/pharmahub/infrastructure/mapper/`**
- Purpose: Map BigQuery `FieldValueList` rows to domain objects (DTOs or generic Maps)
- Contains: Interface RowMapper, BigQueryValues (null-safe column extraction helpers), GenericMapMapper (default, positional keys), 15 query-specific mappers
- Patterns: Strategy pattern — executor looks up mapper by @Named annotation; fallback to generic

**`src/main/resources/`**
- Purpose: Runtime configuration and query definitions
- Contains: application.properties (base), application-*.properties (profile-specific), queries/*/metadata.yaml + query.sql
- Patterns: YAML for query metadata, SQL files for query templates

**`src/main/resources/queries/*/`**
- Purpose: Each directory contains one query's definition
- Structure: metadata.yaml (parameters, pagination, dto/mapper class names) + query.sql (a single BigQuery table-function call, template with `@param` placeholders)
- Count: 15 queries (sales-summary, top-sellers, top-products, stock-search, stock-metrics, sales-overview, sales-comparison, idle-stock, abc-curve-summary, abc-curve-products, stock-without-sales, items-sold-below-cost, manufacturer-sales, products-loss, top-products-by-category)

**`src/main/docker/`**
- Purpose: Container build configurations (Docker images)
- Contains: Dockerfile.jvm (JVM), Dockerfile.native (native binary), variants
- Usage: Build with Maven profile, target artifact found in target/quarkus-app/

## Key File Locations

**Entry Points:**
- `src/main/java/com/rmfarma/pharmahub/api/resource/QueryExecutionResource.java`: POST /queries/{key}/execute
- `src/main/java/com/rmfarma/pharmahub/api/resource/QueryCatalogResource.java`: GET /queries, GET /queries/{key}
- `src/main/java/com/rmfarma/pharmahub/api/resource/HealthResource.java`: GET /health (no auth)
- `src/main/java/com/rmfarma/pharmahub/api/OpenApiConfig.java`: OpenAPI spec + Swagger UI config

**Configuration:**
- `src/main/resources/application.properties`: Base config (port, pagination defaults)
- `src/main/resources/application-dev.properties`: Development overrides (GCP project-id for BigQuery jobs/Secret Manager)
- `src/main/resources/application-prod.properties`: Production overrides
- `src/main/java/com/rmfarma/pharmahub/infrastructure/config/ApiKeyConfig.java`: Loads API keys
- `src/main/java/com/rmfarma/pharmahub/infrastructure/config/QueryHubConfig.java`: Loads pagination/timeout settings

**Core Logic:**
- `src/main/java/com/rmfarma/pharmahub/application/ExecuteQueryUseCase.java`: Query execution orchestration
- `src/main/java/com/rmfarma/pharmahub/infrastructure/query/FileSystemQueryRepository.java`: Query loading
- `src/main/java/com/rmfarma/pharmahub/infrastructure/bigquery/BigQueryQueryExecutor.java`: Table-function execution with pagination
- `src/main/java/com/rmfarma/pharmahub/infrastructure/bigquery/BigQueryParamResolver.java`: Named parameter resolution (`@param`)

**Testing:**
- `src/test/java/com/rmfarma/pharmahub/`: Test classes (currently none; ready for expansion)

**Query Definitions:**
- `src/main/resources/queries/{query-key}/metadata.yaml`: Query parameters, pagination, DTO/mapper class names
- `src/main/resources/queries/{query-key}/query.sql`: Single BigQuery table-function call, template with `@param` placeholders

## Naming Conventions

**Files:**
- **Classes:** PascalCase (e.g., QueryExecutionResource, BigQueryQueryExecutor)
- **Test classes:** {ClassName}Test (e.g., ExecuteQueryUseCaseTest)
- **Configuration files:** application.properties, application-{profile}.properties
- **Query directories:** kebab-case (e.g., sales-summary, abc-curve-products)
- **SQL files:** query.sql (standard name in each query directory)
- **Metadata files:** metadata.yaml (standard name in each query directory)

**Directories:**
- **Source code:** camelCase (main, java, resources, test)
- **Package names:** lowercase, reverse domain notation (com.rmfarma.pharmahub)
- **Layer packages:** api, application, core, infrastructure (singular nouns)
- **Sub-packages:** Descriptive plural or singular (resource, dto, filter, model, port, query, bigquery, mapper, config)
- **Query directories:** kebab-case (sales-summary, top-sellers)

**Classes:**
- **Resources:** {Entity}Resource (e.g., QueryExecutionResource, QueryCatalogResource)
- **Use Cases:** {Action}UseCase (e.g., ExecuteQueryUseCase, ListQueriesUseCase)
- **Repository:** {Entity}Repository (e.g., QueryRepository — interface; FileSystemQueryRepository — impl)
- **Executor:** {Task}Executor (e.g., QueryExecutor, BigQueryQueryExecutor)
- **Mapper:** {Type}Mapper (e.g., SalesSummaryMapper — specific; GenericMapMapper — fallback)
- **Config:** {Domain}Config (e.g., ApiKeyConfig, QueryHubConfig)
- **Exception:** {Scenario}Exception (e.g., QueryNotFoundException, ParamValidationException)
- **Filter:** {Concern}Filter (e.g., ApiKeyFilter)
- **DTO:** {Entity}{Operation}DTO or {Entity}Response (e.g., SalesSummaryDTO, PagedResponse<T>)

**Packages:**
- **api:** REST layer (resources, DTOs, filters, exception handlers)
- **application:** Business logic (use cases, facades)
- **core:** Domain (models, ports, exceptions)
- **infrastructure:** Technical details (database, configuration, mapping)

## Where to Add New Code

**New Query (most common):**
1. Create directory: `src/main/resources/queries/{query-key}/`
2. Add metadata file: `src/main/resources/queries/{query-key}/metadata.yaml` (see sales-summary/metadata.yaml for structure)
3. Add SQL file: `src/main/resources/queries/{query-key}/query.sql`
4. Create DTO: `src/main/java/com/rmfarma/pharmahub/api/dto/response/queries/{QueryNameDTO}.java`
5. Create Mapper: `src/main/java/com/rmfarma/pharmahub/infrastructure/mapper/queries/{QueryNameMapper}.java`
6. Register query key: Add to FileSystemQueryRepository.QUERY_KEYS array (line 22)
7. Annotate mapper: `@Named("{query-key}")` to enable auto-discovery

**New Use Case / Business Logic:**
- Create file: `src/main/java/com/rmfarma/pharmahub/application/{ActionName}UseCase.java`
- Implement: @ApplicationScoped class with clear single responsibility
- Inject: Dependencies via constructor (ports from core)
- Throw: Domain exceptions from core/exception/

**New API Endpoint:**
- Create/edit: `src/main/java/com/rmfarma/pharmahub/api/resource/{EntityName}Resource.java`
- Annotate: @Path, @GET/@POST/@PUT/@DELETE, @Produces(APPLICATION_JSON)
- Add OpenAPI: @Operation, @APIResponse examples
- Inject: Use cases via @Inject
- Security: ApiKeyFilter handles authentication automatically

**New Domain Model:**
- Create: `src/main/java/com/rmfarma/pharmahub/core/model/{EntityName}.java`
- Style: Use record classes for immutability
- No dependencies: Keep core layer free of external libraries

**New Port (Interface):**
- Create: `src/main/java/com/rmfarma/pharmahub/core/port/{CapabilityName}.java`
- Define: Method signatures for capability
- Implement: In infrastructure/ layer with @ApplicationScoped

**New Infrastructure Adapter:**
- Create: `src/main/java/com/rmfarma/pharmahub/infrastructure/{feature}/{AdapterName}.java`
- Implement: Interface from core/port/
- Annotate: @ApplicationScoped (singleton)
- Inject: External dependencies (datasource, config, etc.)

**Configuration Changes:**
- Shared settings: `src/main/resources/application.properties`
- Dev only: `src/main/resources/application-dev.properties`
- Prod only: `src/main/resources/application-prod.properties` (use env vars)
- Load via: Create config class in `src/main/java/com/rmfarma/pharmahub/infrastructure/config/`

## Special Directories

**`target/`**
- Purpose: Maven build output
- Generated: Yes (mvn clean package)
- Committed: No (.gitignore)
- Contains: Compiled classes, JARs, native binary, Docker image tars, quarkus-app/

**`graphify-out/`**
- Purpose: Output from code analysis tooling (graphify)
- Generated: Yes (external tool)
- Committed: No (.gitignore)
- Contains: AST cache, analysis results

**`.planning/codebase/`**
- Purpose: GSD codebase mapping documents
- Generated: Yes (by /gsd:map-codebase)
- Committed: Yes (tracking codebase state)
- Contains: ARCHITECTURE.md, STRUCTURE.md, CONVENTIONS.md, TESTING.md, CONCERNS.md

---

*Structure analysis: 2026-08-06*
