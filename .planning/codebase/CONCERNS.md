# Codebase Concerns

**Analysis Date:** 2026-08-06

## Test Coverage Gaps

**Complete Absence of Tests:**
- What's not tested: All business logic, API endpoints, database interactions, and error scenarios
- Files: All source code in `src/main/java/com/rmfarma/pharmahub/` — test directory at `src/test/java/` is empty
- Risk: Critical issues can reach production undetected. Refactoring is extremely risky. No regression protection for bug fixes.
- Priority: **HIGH**

**Impact:** Impossible to safely refactor, add features, or debug issues. Query execution logic, parameter validation, pagination, and exception handling have zero safety net.

## Security Considerations

**API Key Authentication Timing Attack:**
- Risk: `ApiKeyFilter.resolveClientId()` at `src/main/java/com/rmfarma/pharmahub/api/filter/ApiKeyFilter.java:60` uses `.equals()` for comparing API keys, which is vulnerable to timing attacks
- Files: `src/main/java/com/rmfarma/pharmahub/api/filter/ApiKeyFilter.java` (line 61)
- Current mitigation: None
- Recommendations: Use constant-time comparison (e.g., `MessageDigest.isEqual()` or a crypto library)

**Exception Messages Leak Sensitive Data:**
- Risk: Database error messages exposed to clients
- Files: 
  - `src/main/java/com/rmfarma/pharmahub/api/exception/GlobalExceptionMapper.java:42` — exposes `exception.getMessage()` in 500 responses
  - `src/main/java/com/rmfarma/pharmahub/api/resource/HealthResource.java:83` — leaks database connection error details
- Current mitigation: None
- Recommendations: Log full errors server-side, return generic messages to clients (e.g., "Internal server error" without exception details)

**API Keys Visible in OpenAPI Documentation:**
- Risk: Dev API keys exposed in public Swagger UI documentation
- Files: `src/main/resources/application-dev.properties:55,60` — keys visible in `quarkus.smallrye-openapi.info-description` and `quarkus.smallrye-openapi.security-scheme-description`
- Current mitigation: Swagger UI disabled in production (`application-prod.properties:35`)
- Recommendations: Remove specific key examples from description fields. Document that API keys come from Secret Manager, not hardcoded.

**Credentials in Version Control:**
- Risk: `env.yaml` contains database credentials and is modified in git status
- Files: `env.yaml` (currently staged as "M" in git status)
- Current mitigation: Listed in `.gitignore` (but currently tracked)
- Recommendations: Remove from git history using `git rm --cached env.yaml`. Use `.env.example` for template only. Ensure only env vars are used in development.

## Performance Bottlenecks

**API Key Lookup is O(n):**
- Problem: Every request iterates through all API keys to validate authentication
- Files: `src/main/java/com/rmfarma/pharmahub/api/filter/ApiKeyFilter.java:60-66`
- Cause: Linear iteration through `apiKeyConfig.apiKeys().entrySet()` on every request
- Improvement path: Convert to HashMap-based lookup in `ApiKeyConfig`. Build a reverse map (`key → clientId`) at initialization and use O(1) lookup.

**COUNT Query Wraps Entire Result Set:**
- Problem: Very slow pagination counting for large datasets
- Files: `src/main/java/com/rmfarma/pharmahub/infrastructure/db/JdbcQueryExecutor.java:99`
- Cause: `SELECT COUNT(*) FROM (original_query) AS count_query` approach requires full query execution
- Improvement path: Consider alternatives:
  - Add database indexes on frequently sorted/filtered columns
  - Use approximate row counts from `pg_stat_user_tables` (PostgreSQL)
  - Implement cursor-based pagination instead of offset-based
  - Cache result counts for predictable queries

## Fragile Areas

**Unhandled Integer Parsing in Configuration:**
- Files: `src/main/java/com/rmfarma/pharmahub/infrastructure/query/FileSystemQueryRepository.java:122`
- Why fragile: `Integer.parseInt(s)` throws `NumberFormatException` if metadata YAML contains invalid integer values (e.g., `defaultPageSize: "not-a-number"`)
- Safe modification: Wrap parsing in try-catch, provide clear error message during startup
- Test coverage: MISSING

**Generic Exception Wrapping Hides Root Causes:**
- Files: `src/main/java/com/rmfarma/pharmahub/infrastructure/db/JdbcQueryExecutor.java:115-117, 142-144`
- Why fragile: Converts all `SQLException` to generic `RuntimeException`, losing specific error context
- Safe modification: Create custom exceptions for different failure scenarios (e.g., `QueryTimeoutException`, `ConnectionPoolExhaustedException`)
- Test coverage: MISSING

**No Explicit Timeout Exception Handling:**
- Files: `src/main/java/com/rmfarma/pharmahub/infrastructure/db/JdbcQueryExecutor.java:105,132` — sets timeout but no specific handling
- Why fragile: SQLTimeoutException is wrapped as generic RuntimeException, making it hard to distinguish timeout from other failures
- Safe modification: Catch `SQLTimeoutException` separately and throw custom exception with client-friendly message

**Query Execution Assumes SQL Validity:**
- Files: All `.sql` files in `src/main/resources/queries/*/query.sql`
- Why fragile: No validation that SQL files are syntactically correct until first execution
- Safe modification: Validate SQL during application startup (parse or dry-run against database schema)

## Missing Critical Features

**No Rate Limiting:**
- Problem: No protection against resource exhaustion via repeated large query requests
- Blocks: Cannot safely expose API in production environments with untrusted clients
- Missing: Endpoint-level rate limiting, user-level quotas, or circuit breakers

**No Request/Response Logging Middleware:**
- Problem: Difficult to debug production issues or analyze usage patterns
- Blocks: Cannot diagnose performance issues or identify problematic queries
- Missing: Comprehensive request logging (not just errors) with query execution metrics

**No Query Result Caching:**
- Problem: Identical repeated queries hit database every time
- Blocks: No optimization for frequently-run stable queries
- Missing: Caching layer for query results with TTL and invalidation strategy

## Architectural Concerns

**Large OpenAPI Documentation in Controller:**
- Files: `src/main/java/com/rmfarma/pharmahub/api/resource/QueryExecutionResource.java` — 585 lines, ~450 lines are annotations
- Problem: Business logic obscured by extensive OpenAPI annotations. Hard to modify endpoints.
- Fix approach: Extract OpenAPI generation to separate documentation class or use external spec file (YAML). Keep controller focused on HTTP handling.

**No Structured Logging:**
- Files: All files using `org.jboss.logging.Logger`
- Problem: Uses `LOG.infov()` and `LOG.debugv()` without structured fields for machine parsing
- Impact: Makes correlation of related log entries difficult, especially across different services
- Recommendation: Emit JSON logs with requestId, clientId, queryKey fields consistently

## Tech Debt

**Hardcoded Query Registry:**
- Files: `src/main/java/com/rmfarma/pharmahub/infrastructure/query/FileSystemQueryRepository.java:22-26`
- Issue: Query keys are hardcoded array instead of discovered from filesystem
- Impact: New queries require code change. Query loading order is implicit.
- Fix approach: Scan `queries/` directory at startup. Validate all required files exist.

**No Schema Validation for SQL Result Sets:**
- Files: `src/main/java/com/rmfarma/pharmahub/infrastructure/mapper/` — multiple mapper classes
- Issue: Each query has custom mapper. No validation that result set schema matches expected DTO
- Impact: Field renaming in SQL breaks at runtime, not compile time
- Fix approach: Generate mappers from DTOs using annotation processor or reflection-based mapper factory

**Global Exception Mapper Creates New UUID Per Error:**
- Files: `src/main/java/com/rmfarma/pharmahub/api/exception/GlobalExceptionMapper.java:22`
- Issue: Each error gets a random requestId, breaking request tracing between filter and exception handler
- Impact: Cannot correlate API Key rejection (in filter) with exception responses
- Fix approach: Generate requestId in filter or context, reuse in exception mapper

**Magic Numbers in Configuration:**
- Files: Throughout — timeouts, page sizes, row limits hardcoded in annotations and code
- Issue: Constants scattered across `QueryDefinition`, `application.properties`, metadata YAML
- Impact: Hard to audit or adjust defaults across codebase
- Fix approach: Centralize all configuration in single config class with documented defaults

## Dependencies at Risk

**SnakeYAML Configuration Parsing:**
- Risk: Unsafe YAML parsing could be exploited if metadata files are controlled by untrusted sources
- Files: `src/main/java/com/rmfarma/pharmahub/infrastructure/query/FileSystemQueryRepository.java:32`
- Current: Uses `new Yaml()` with default settings
- Migration plan: Use `Yaml.constructor(new SafeConstructor())` to prevent arbitrary object instantiation

**Limited Parameter Type Support:**
- Risk: Only supports STRING, INTEGER, LONG, DECIMAL, BOOLEAN, DATE, TIMESTAMP
- Files: `src/main/java/com/rmfarma/pharmahub/core/model/ParamType.java`
- Impact: Cannot handle UUID, JSON, Array types. Workaround is passing as strings.
- Recommendation: Extend to support more types or document current limitations

---

*Concerns audit: 2026-08-06*
