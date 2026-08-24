# Coding Conventions

**Analysis Date:** 2026-08-06

## Naming Patterns

**Classes:**
- PascalCase with descriptive names: `QueryExecutionResource`, `ExecuteQueryUseCase`, `TopSellerMapper`
- REST resources end with `Resource`: `QueryExecutionResource`, `QueryCatalogResource`, `HealthResource`
- Use cases end with `UseCase`: `ExecuteQueryUseCase`, `ListQueriesUseCase`, `GetQueryDetailsUseCase`
- Mappers end with `Mapper`: `TopSellerMapper`, `RowMapper`, `GenericMapMapper`
- Exception classes end with `Exception`: `QueryNotFoundException`, `ParamValidationException`
- Configuration classes end with `Config`: `ApiKeyConfig`, `QueryHubConfig`

**Methods/Variables:**
- camelCase: `executePaged()`, `validateAndResolveParams()`, `apiKeys`, `requestId`, `pageSize`
- Private helper methods use camelCase with descriptive verbs: `stripTrailingLimit()`, `resolveClientId()`
- Boolean methods/fields use `is`, `has`, `allow` prefixes: `allowUnpaged()`, `hasTrailingLimit()`, `truncated`

**Constants:**
- UPPER_SNAKE_CASE: `TRAILING_LIMIT_PATTERN`, `API_KEY_HEADER`, `CLIENT_ID_HEADER`, `DEFAULT_PAGE_SIZE`
- Static final for logger declaration: `private static final Logger LOG = Logger.getLogger(Class.class);`

**Records/Records:**
- Use record keyword for immutable DTOs: `ExecuteRequest`, `PagedResponse<T>`, `ErrorResponse`
- Generic type parameters in angle brackets: `PagedResponse<T>`, `RowMapper<T>`

## Code Style

**Records/Immutable Data:**
- Use Java records for request/response DTOs and model objects
- Records are compact and auto-generate constructor, getters, equals, hashCode, toString
- Example: `public record ExecuteRequest(Map<String, Object> params, Integer page, Integer pageSize, Boolean unpaged) { }`
- Inner record classes for complex return types: See `ExecuteQueryUseCase.ExecutionResult`

**Method Parameters:**
- Use standard types: `String`, `Map<String, Object>`, `Integer`, `List<T>`
- Accept `null` for optional parameters, validate explicitly: `if (page != null && page >= 1) ? page : 1`
- Use `Boolean.TRUE.equals()` for null-safe boolean comparison instead of simple `==`

**String Handling:**
- Multi-line strings use triple quotes (text blocks):
  ```java
  String description = """
      Line 1
      Line 2
      """;
  ```
- String interpolation via `.formatted()` for parameterized strings:
  ```java
  "Parâmetro '%s' com valor inválido para tipo %s: %s".formatted(name, type, value)
  ```
- String concatenation with `+` for simple cases

**Resource Management:**
- Try-with-resources for database connections: `try (Connection conn = dataSource.getConnection(); ...)`
- Automatic resource cleanup prevents leaks

**Enums:**
- Abstract enum patterns for polymorphic behavior:
  ```java
  public enum ParamType {
      STRING {
          @Override
          public Object convert(String value) { ... }
      },
      INTEGER {
          @Override
          public Object convert(String value) { ... }
      }
      // ...abstract methods
  }
  ```

## Import Organization

**Order:**
1. Jakarta EE/Quarkus imports: `jakarta.*`, `org.eclipse.microprofile.*`
2. Standard Java imports: `java.util.*`, `java.sql.*`, etc.
3. Project imports: `com.rmfarma.pharmahub.*`

**Example:**
```java
import com.rmfarma.pharmahub.api.dto.request.ExecuteRequest;
import com.rmfarma.pharmahub.application.ExecuteQueryUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.*;
import java.util.Map;
import java.util.UUID;
```

**Path Aliases:**
- No aliases configured; full package paths used throughout

## Error Handling

**Custom Exceptions:**
- Extend `RuntimeException` (unchecked) for domain-specific errors
- Simple constructors with clear error messages in Portuguese:
  ```java
  public class QueryNotFoundException extends RuntimeException {
      public QueryNotFoundException(String queryKey) {
          super("Query não encontrada: " + queryKey);
      }
  }
  ```

**Exception Mapping:**
- `GlobalExceptionMapper` handles all exceptions and converts to HTTP responses
- Location: `com.rmfarma.pharmahub.api.exception.GlobalExceptionMapper`
- Maps specific exceptions to HTTP status codes:
  - `QueryNotFoundException` → 404
  - `ParamValidationException` → 422
  - `UnpagedNotAllowedException` → 403
  - Generic `Throwable` → 500

**Error Response Format:**
- All errors return `ErrorResponse(error, message, details, requestId)` record
- Fields are: error code, user message, technical details, request tracking ID

## Logging

**Framework:** Quarkus JBoss Logging

**Logger Declaration:**
```java
private static final Logger LOG = Logger.getLogger(ClassName.class);
```

**Log Levels & Patterns:**

| Level | Method | When to Use | Example |
|-------|--------|------------|---------|
| DEBUG | `LOG.debug()`, `LOG.debugv()` | SQL queries, parameter bindings | `LOG.debugv("Executando SQL: {0}", sql)` |
| INFO | `LOG.info()`, `LOG.infov()` | Query execution, application startup | `LOG.infov("Query carregada: {0}", key)` |
| WARN | `LOG.warn()`, `LOG.warnv()` | Missing API keys, invalid params | `LOG.warn("Requisição sem API Key")` |
| ERROR | `LOG.error()`, `LOG.errorv()` | Unhandled exceptions, DB connection failures | `LOG.errorv(exception, "Message with {0}", param)` |

**Parameterized Logging:**
- Use `LOG.infov()`, `LOG.debugv()`, etc. for parameterized messages
- Placeholders: `{0}`, `{1}`, `{2}`, etc.
- Example: `LOG.infov("Query executed: key={0}, mode={1}, rows={2}", key, mode, rowCount)`

**Business Events Logged:**
- Query execution details: key, mode (PAGED/UNPAGED), duration, rows returned
- Security events: missing/invalid API keys
- Database errors with context

## Comments

**When to Comment:**
- Complex algorithms or regex patterns
- Non-obvious business logic
- Important constraints or limitations

**Examples from Codebase:**
```java
/**
 * Detecta LIMIT no final do SQL (hardcoded ou com named param @limit).
 * Ignora LIMIT dentro de subqueries — só faz match no LIMIT mais externo no final do SQL.
 */
private static final Pattern TRAILING_LIMIT_PATTERN = ...

// Se o SQL já tem LIMIT (hardcoded ou @limit param), não adicionar outro
if (!hasTrailingLimit(sql)) {
    sql = sql + "\nLIMIT " + (maxRows + 1);
}
```

**Note (2026-08-24 BigQuery migration):** named parameters in `query.sql` templates changed syntax from JDBC-style `:param` to BigQuery-style `@param` — this is a hard requirement of the BigQuery client API, not a style choice.

**Language:** Portuguese (pt-BR) for comments and documentation

**JavaDoc:**
- Use `/** ... */` blocks for public classes and public methods
- Include parameter descriptions when not obvious
- Document exceptions thrown (especially custom exceptions)

## Function Design

**Size:**
- Methods should be focused and single-purpose
- Use helper methods to keep functions under ~50 lines
- Example: `validateAndResolveParams()` is extracted from `execute()` for clarity

**Parameters:**
- Use position-based parameters, no builder patterns
- Maximum 4-5 parameters; use record classes for complex parameter sets
- Accept `Map<String, Object>` for flexible parameter passing

**Return Values:**
- Use appropriate types: primitives for simple values, records for multiple values
- Return records or `Optional` for nullable returns (though not commonly used here)
- Void for side-effect operations

**Method Naming:**
- Verb-noun pattern: `executePaged()`, `validateAndResolveParams()`, `resolveClientId()`
- Getter pattern: no "get" prefix for records (auto-generated), use method names directly
- Query methods use `is`/`has`/`allow` patterns: `hasTrailingLimit()`, `allowUnpaged()`

## Module Design

**Exports:**
- Classes marked as resource/service: `@Path`, `@ApplicationScoped`, `@Provider` annotations
- Dependency injection via constructor in most classes
- Clean separation between API, Application, Core, and Infrastructure layers

**Barrel Files:**
- No barrel/index files configured
- Import directly from specific classes

**Dependency Injection:**
- Quarkus CDI (Context & Dependency Injection)
- Constructor-based injection preferred: `public ClassName(Dependency dep1, Dependency dep2) { ... }`
- Field injection with `@Inject` used for configuration classes
- `@ApplicationScoped` for singleton beans that need state
- `@Named` for qualifying bean names: `@Named("top-sellers")`

**Annotations for Component Declaration:**
- `@ApplicationScoped`: Singleton managed beans
- `@Provider`: JAX-RS providers (filters, exception mappers)
- `@Path`: REST endpoints
- `@Named`: Qualifier for CDI beans

## Testing Annotations

**Test Framework Configured (Not Yet Implemented):**
- `@QuarkusTest` for Quarkus test support (quarkus-junit5 dependency present)
- No tests currently exist in `src/test/java/`
- REST Assured available for integration testing

---

*Convention analysis: 2026-08-06*
