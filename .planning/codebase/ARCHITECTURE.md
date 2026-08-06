# Architecture

**Analysis Date:** 2026-08-06

## System Overview

Pharma Hub is a REST API for executing pre-approved SQL analytics queries on a PostgreSQL database. It provides an abstraction layer over 11 predefined queries with support for pagination, typed parameters, and API key authentication.

```text
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                           │
│  ┌──────────────────┬──────────────┬──────────────────────┐ │
│  │ QueryExecution   │QueryCatalog  │    HealthResource    │ │
│  │   Resource       │   Resource   │   (no auth required) │ │
│  │ `api/resource/`  │ `api/resource│  `api/resource/`    │ │
│  └─────────────────┬┴──────────┬───┴──────────────────────┘ │
└────────────────────┼───────────┼──────────────────────────────┘
                     │ depends   │
┌────────────────────▼───────────▼──────────────────────────────┐
│            API Filters & Exception Handling                   │
│  ┌─────────────────┬─────────────────┬──────────────────────┐ │
│  │ ApiKeyFilter    │ GlobalException │ OpenApiConfig        │ │
│  │ (pre-matching)  │    Mapper       │ (SmallRye OpenAPI)   │ │
│  │ `api/filter/`   │ `api/exception` │ `api/`               │ │
│  └────────────────┬┴────────────┬────┴──────────────────────┘ │
└───────────────────┼─────────────┼──────────────────────────────┘
                    │ delegates   │
┌───────────────────▼─────────────▼──────────────────────────────┐
│          Application Layer (Use Cases)                         │
│  ┌──────────────────┬──────────────┬──────────────────────────┐
│  │ ExecuteQuery     │ ListQueries  │ GetQueryDetails          │
│  │  UseCase         │  UseCase     │  UseCase                 │
│  │ `application/`   │`application/ │ `application/`           │
│  └────────┬─────────┴──────┬───────┴──────────┬───────────────┘
│           │ depends on     │                  │               │
└───────────┼────────────────┼──────────────────┼───────────────┘
            │ Ports (abstractions) in `core/port/`
┌───────────▼────────────────────────────────────────────────────┐
│  Core Domain Layer                                              │
│  ┌────────────────────┬──────────────────────────────────────┐ │
│  │ QueryRepository    │ QueryExecutor (interface)            │ │
│  │ (find by key)      │ - executePaged(definition, params..) │ │
│  │ (find all)         │ - executeUnpaged(definition, params) │ │
│  └────────────────────┴──────────────────────────────────────┘ │
│  Models: QueryDefinition, ParamDefinition, ExecutionMode       │
│  Results: PagedResult<T>, UnpagedResult<T>                     │
│  Exceptions: QueryNotFoundException, ParamValidationException  │
│  `core/model/`, `core/port/`, `core/exception/`                │
└─────────────────────────────────────────────────────────────────┘
            │ realized by
┌───────────▼──────────────────────────────────────────────────────┐
│          Infrastructure Layer                                    │
│  ┌──────────────────┬─────────────────┬──────────────────────┐  │
│  │ FileSystem       │  JdbcQuery      │ Mappers & Config     │  │
│  │ QueryRepository  │  Executor       │                      │  │
│  │ (loads YAML      │ (PostgreSQL)    │ - Result mapping     │  │
│  │  + SQL from      │ - Parameter     │ - Query config       │  │
│  │  classpath)      │   resolution    │ - API Key config     │  │
│  │                  │ - Pagination    │                      │  │
│  │                  │ - Named param   │                      │  │
│  │                  │   binding       │                      │  │
│  │ `query/`         │ `db/`           │ `mapper/`, `config/` │  │
│  └──────────────────┴─────────────────┴──────────────────────┘  │
│                         ▼                                        │
│                  PostgreSQL Database                             │
│                  (bq_licenciado_rel schema)                      │
└────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| QueryExecutionResource | HTTP POST /queries/{key}/execute endpoint | `api/resource/QueryExecutionResource.java` |
| QueryCatalogResource | HTTP GET /queries (list) and GET /queries/{key} (details) endpoints | `api/resource/QueryCatalogResource.java` |
| HealthResource | HTTP GET /health (no auth) | `api/resource/HealthResource.java` |
| ApiKeyFilter | Pre-matching filter for X-API-Key validation | `api/filter/ApiKeyFilter.java` |
| GlobalExceptionMapper | Maps domain exceptions to HTTP responses | `api/exception/GlobalExceptionMapper.java` |
| ExecuteQueryUseCase | Coordinates query execution: loads definition, validates params, determines mode | `application/ExecuteQueryUseCase.java` |
| ListQueriesUseCase | Fetches all available queries from repository | `application/ListQueriesUseCase.java` |
| GetQueryDetailsUseCase | Fetches single query definition by key | `application/GetQueryDetailsUseCase.java` |
| FileSystemQueryRepository | Loads query metadata (YAML) and SQL from classpath | `infrastructure/query/FileSystemQueryRepository.java` |
| JdbcQueryExecutor | Executes SQL with pagination support against PostgreSQL | `infrastructure/db/JdbcQueryExecutor.java` |
| NamedParamResolver | Converts :param placeholder SQL to ? placeholders + type bindings | `infrastructure/db/NamedParamResolver.java` |
| ResultSetMapper (interface) | Converts ResultSet to domain objects (DTO or Map) | `infrastructure/mapper/ResultSetMapper.java` |
| GenericMapMapper | Default mapper: converts ResultSet rows to Map<String, Object> | `infrastructure/mapper/GenericMapMapper.java` |
| Specific Mappers (11x) | Query-specific mappers (e.g., SalesSummaryMapper) | `infrastructure/mapper/queries/*.java` |

## Pattern Overview

**Overall:** Hexagonal Architecture (Ports & Adapters) with layered separation

**Key Characteristics:**
- **Domain-driven:** Core layer defines all interfaces (ports) that drive the architecture
- **Dependency inversion:** Application layer depends on core abstractions, not implementations
- **REST-first:** JAX-RS (Quarkus/MicroProfile) with comprehensive OpenAPI documentation
- **Query-as-data:** Queries are defined declaratively in YAML + SQL files, loaded at startup
- **Flexible output:** Supports both strong-typed mappers (DTO) and generic Map output
- **Pagination abstraction:** Two execution modes (PAGED/UNPAGED) handled transparently

## Layers

**API Layer (`api/`):**
- Purpose: HTTP request/response handling and OpenAPI documentation
- Location: `api/resource/`, `api/filter/`, `api/exception/`, `api/dto/`
- Contains: JAX-RS Resource classes, request/response DTOs, exception handlers, filters
- Depends on: Application layer (use cases), core models
- Used by: HTTP clients via REST

**Application Layer (`application/`):**
- Purpose: Business logic and orchestration (use cases)
- Location: `application/`
- Contains: Use case classes implementing core functionality
- Depends on: Core ports (interfaces), domain models
- Used by: API resources

**Core Layer (`core/`):**
- Purpose: Domain entities and abstractions (DDD bounded context)
- Location: `core/model/`, `core/port/`, `core/exception/`
- Contains: Record classes (models), interfaces (ports), domain exceptions
- Depends on: Nothing (pure domain, no external dependencies)
- Used by: All other layers

**Infrastructure Layer (`infrastructure/`):**
- Purpose: Technical implementations (database, mapping, configuration)
- Location: `infrastructure/query/`, `infrastructure/db/`, `infrastructure/mapper/`, `infrastructure/config/`
- Contains: Repository implementation, JDBC executor, ResultSet mappers, config classes
- Depends on: Core ports, external libraries (Quarkus, JDBC, YAML)
- Used by: Application layer via ports

## Data Flow

### Primary Request Path (Query Execution)

1. **HTTP Request arrives** (`api/resource/QueryExecutionResource.execute()` — line 539)
   - POST /queries/{key}/execute with ExecuteRequest body
   
2. **Authentication & Validation** (`api/filter/ApiKeyFilter.filter()` — line 27)
   - Pre-matching filter intercepts all requests
   - Validates X-API-Key header
   - Injects X-Client-Id for logging (resolves from ApiKeyConfig)
   - Allows /health and /q/* endpoints without auth

3. **Use Case Invocation** (`application/ExecuteQueryUseCase.execute()` — line 35)
   - Fetch QueryDefinition from repository: `queryRepository.findByKey(key)`
   - Validate and resolve parameters: `validateAndResolveParams()`
   - Determine execution mode: PAGED (default) or UNPAGED (if requested)

4. **Query Execution** (`infrastructure/db/JdbcQueryExecutor.executePaged()` or `.executeUnpaged()`)
   - For PAGED:
     - Execute COUNT query: `executeCount()` with subquery wrapping (line 62)
     - Strip LIMIT from original SQL
     - Append LIMIT/OFFSET for page
   - For UNPAGED:
     - Append LIMIT (maxRows + 1) to detect truncation
   - Resolve named parameters: `NamedParamResolver.resolve()` converts :param to ?
   - Bind parameters to PreparedStatement with type coercion
   
5. **Result Mapping** (`infrastructure/mapper/*.java`)
   - Look up query-specific mapper by key (via @Named annotation)
   - If found: use specific mapper (e.g., SalesSummaryMapper)
   - If not found: use GenericMapMapper (returns Map<String, Object>)
   - Map each ResultSet row to domain object

6. **Response Building** (`api/resource/QueryExecutionResource.execute()` — line 556)
   - Wrap results in PagedResponse or UnpagedResponse
   - Include metadata: queryKey, mode, page, pageSize, totalItems, totalPages, durationMs, requestId
   - Add truncation flag/message if applicable

7. **Exception Handling** (`api/exception/GlobalExceptionMapper.toResponse()` — line 21)
   - QueryNotFoundException → 404
   - ParamValidationException → 422
   - UnpagedNotAllowedException → 403
   - MaxRowsExceededException → 422
   - Any other Throwable → 500 with requestId

### Secondary Flows

**Query Catalog Listing:**
1. GET /queries → QueryCatalogResource.listQueries()
2. ListQueriesUseCase.execute() → queryRepository.findAll()
3. FileSystemQueryRepository returns all loaded queries
4. Convert each QueryDefinition to QueryInfoResponse
5. Return JSON array with all query metadata

**Query Details:**
1. GET /queries/{key} → QueryCatalogResource.getQueryDetails()
2. GetQueryDetailsUseCase.execute(key) → queryRepository.findByKey(key)
3. Throw QueryNotFoundException if not found
4. Convert QueryDefinition to QueryInfoResponse
5. Return single query metadata

**Health Check:**
1. GET /health → HealthResource.health()
2. Test database connectivity with SELECT 1
3. Return {status: UP/DOWN, database: connected/disconnected}
4. No authentication required

**State Management:**
- **Stateless HTTP:** Each request is independent, no session state
- **Shared state:** QueryDefinitions cached in-memory by FileSystemQueryRepository after @PostConstruct init
- **Thread-safe:** Quarkus/ArC dependency injection manages singleton scopes
- **Query metadata:** Immutable (record classes) — no mutations during request

## Key Abstractions

**QueryDefinition:**
- Purpose: Encapsulates all metadata for a single query
- Examples: `core/model/QueryDefinition.java`
- Pattern: Record (immutable data class) with 14 fields: key, version, description, endpoint, tags, sqlTemplate, params, pagination settings, dto/mapper class names

**QueryRepository:**
- Purpose: Abstract the source of query metadata
- Examples: `core/port/QueryRepository.java` (interface), `infrastructure/query/FileSystemQueryRepository.java` (implementation)
- Pattern: Port & Adapter — repository interface defined in core, filesystem adapter in infrastructure

**QueryExecutor:**
- Purpose: Abstract SQL execution and pagination logic
- Examples: `core/port/QueryExecutor.java` (interface), `infrastructure/db/JdbcQueryExecutor.java` (implementation)
- Pattern: Generic interface with type parameter <T> to support any result type

**ExecutionResult:**
- Purpose: Return both the result and execution metadata
- Examples: `application/ExecuteQueryUseCase.ExecutionResult` (record)
- Pattern: Wrapper record carrying mode (PAGED/UNPAGED), result object, and query definition

**ParamType:**
- Purpose: Type system for query parameters with conversion and JDBC binding
- Examples: `core/model/ParamType.java` (enum with 7 types: STRING, INTEGER, LONG, DECIMAL, BOOLEAN, DATE, TIMESTAMP)
- Pattern: Strategy enum — each type knows how to convert strings and bind to PreparedStatement

## Entry Points

**HTTP Entry Points:**

**POST /queries/{key}/execute**
- Location: `api/resource/QueryExecutionResource.execute()` (line 539)
- Triggers: Client POST request with ExecuteRequest body
- Responsibilities: 
  - Parse path parameter {key}
  - Extract request headers (X-API-Key, X-Client-Id)
  - Delegate to ExecuteQueryUseCase
  - Format response (PagedResponse or UnpagedResponse)
  - Log execution metrics

**GET /queries**
- Location: `api/resource/QueryCatalogResource.listQueries()` (line 98)
- Triggers: Client GET request
- Responsibilities: List all available queries with metadata

**GET /queries/{key}**
- Location: `api/resource/QueryCatalogResource.getQueryDetails()` (line 169)
- Triggers: Client GET request for specific query
- Responsibilities: Fetch and return single query metadata

**GET /health**
- Location: `api/resource/HealthResource.health()` (line 68)
- Triggers: Client GET request (no auth required)
- Responsibilities: Test database connectivity, return status

**Initialization Entry Point:**

**@PostConstruct init() in FileSystemQueryRepository**
- Location: `infrastructure/query/FileSystemQueryRepository.java` (line 31)
- Triggers: Application startup (Quarkus initialization)
- Responsibilities:
  - Iterate over hardcoded QUERY_KEYS array (line 22)
  - Load metadata.yaml + query.sql for each query from classpath
  - Parse YAML into QueryDefinition
  - Cache in LinkedHashMap<String, QueryDefinition>
  - Log success/failure for each query

## Architectural Constraints

- **Threading:** Single-threaded event loop per request (Quarkus virtual threads); shared QueryDefinition cache is read-only after init
- **Global state:** 
  - FileSystemQueryRepository.queries (LinkedHashMap) — shared, immutable after init
  - ApiKeyConfig.apiKeys (Map) — shared, immutable (from config)
  - Quarkus Arc scopes (@ApplicationScoped) — singleton per JVM
- **Circular imports:** None detected (clean dependency graph: API → Application → Core ← Infrastructure)
- **Query reloading:** NOT supported — queries are loaded once at startup, no runtime refresh
- **Parameter safety:** Named parameters use PreparedStatement binding (SQL injection safe)
- **Result streaming:** NOT used — all results loaded into memory (List<T>)
- **Database connection pool:** Agroal (Quarkus datasource) manages connection pooling

## Anti-Patterns

### Hardcoded Query Keys in FileSystemQueryRepository

**What happens:** Query keys are hardcoded in a static array (line 22: QUERY_KEYS = {...})

**Why it's wrong:** Adding a new query requires code change + recompilation + redeployment. No dynamic discovery.

**Do this instead:** Scan classpath for query folders automatically (Spring ResourceLoader pattern) or implement a plugin/registry system where queries can be added via configuration only.

**Reference:** `infrastructure/query/FileSystemQueryRepository.java:22`

### Manual SQL LIMIT/OFFSET Construction

**What happens:** Pagination is implemented by string manipulation — stripTrailingLimit() + appending LIMIT/OFFSET (line 66)

**Why it's wrong:** Error-prone for complex SQL (subqueries, CTEs); no database-agnostic abstraction

**Do this instead:** Use a query builder library (jOOQ, QueryDSL) or create a DatabaseDialect abstraction to handle SQL generation per database type.

**Reference:** `infrastructure/db/JdbcQueryExecutor.java:34-173`

### Duplicated Mapper Classes for Each Query

**What happens:** 11 separate mapper classes (SalesSummaryMapper, TopProductMapper, etc.) each implementing ResultSetMapper with manual field extraction

**Why it's wrong:** Code duplication; violation of DRY; maintenance burden when schema changes

**Do this instead:** Use reflection-based mapping (Jackson + @JsonProperty) or generate mappers via annotation processor. GenericMapMapper works but lose type safety.

**Reference:** `infrastructure/mapper/queries/*.java` (all similar pattern)

### No Transaction Management

**What happens:** Each query is a separate connection without explicit transaction control

**Why it's wrong:** Read-only queries OK, but if schema/operations change in future, lack of transaction boundaries could cause inconsistency

**Do this instead:** Wrap query execution in @Transactional(readOnly = true) or explicit transaction scope for future safety.

**Reference:** `infrastructure/db/JdbcQueryExecutor.java:54-95`

## Error Handling

**Strategy:** Exception-based, with centralized mapping to HTTP status codes

**Patterns:**
- Domain exceptions defined in `core/exception/` are custom unchecked exceptions
- ApplicationScoped use cases throw domain exceptions (no try-catch)
- GlobalExceptionMapper intercepts Throwable and converts to ErrorResponse (line 46)
- Each domain exception maps to specific HTTP status:
  - QueryNotFoundException → 404 NOT_FOUND
  - ParamValidationException → 422 UNPROCESSABLE_ENTITY
  - UnpagedNotAllowedException → 403 FORBIDDEN
  - MaxRowsExceededException → 422 UNPROCESSABLE_ENTITY
  - Generic Exception → 500 INTERNAL_SERVER_ERROR
- All responses include requestId (UUID) for tracing

## Cross-Cutting Concerns

**Logging:**
- Framework: JBoss Logging (org.jboss.logging.Logger)
- Approach: Explicit LOG calls at key points (use case invocation, query execution, errors)
- Example: `QueryExecutionResource.java:578` — logs query execution with requestId, clientId, mode, duration
- JSON logging available via quarkus-logging-json for production

**Validation:**
- Parameter type validation: ExecuteQueryUseCase.validateAndResolveParams() (line 78)
- Throws ParamValidationException if required param missing or type conversion fails
- Supports optional params with default values (from metadata)

**Authentication:**
- Mechanism: API Key in X-API-Key header
- Implementation: ApiKeyFilter pre-matching filter
- Resolution: ApiKeyConfig maps key → clientId
- Exemptions: /health, /q/* (metrics, dev-ui)

**Authorization:**
- Not implemented — only authentication (API key presence)
- No per-query permission checks
- All authenticated users can access all queries

---

*Architecture analysis: 2026-08-06*
