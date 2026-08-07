# Testing Patterns

**Analysis Date:** 2026-08-06

## Test Framework

**Runner:**
- JUnit 5 (through Quarkus Test support)
- Dependency: `quarkus-junit5` v3.27.2 (managed by Quarkus BOM)
- Config: None (uses default Quarkus conventions)

**Assertion Library:**
- JUnit 5 assertions: `org.junit.jupiter.api.Assertions` (included with JUnit 5)

**HTTP Testing:**
- REST Assured (dependency present: `rest-assured`, test scope)
- For integration tests against REST endpoints

**Run Commands:**

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=QueryExecutionResourceTest

# Run specific test method
./mvnw test -Dtest=QueryExecutionResourceTest#testExecutePaged

# Run with Quarkus dev mode (continuous testing)
./mvnw quarkus:dev

# Run integration tests (native image)
./mvnw verify -Pnative
```

## Test File Organization

**Current State:**
- Test directory exists: `src/test/java/com/rmfarma/pharmahub/`
- **No test files currently exist**

**Location Convention (When Adding Tests):**
- Mirror source structure under `src/test/java/`
- Test classes co-located with production code package structure
- Example: Class `com.rmfarma.pharmahub.api.resource.QueryExecutionResource` would have test class `com.rmfarma.pharmahub.api.resource.QueryExecutionResourceTest`

**Naming Convention:**
- Test class name: `[ClassName]Test`
- Test methods: `test[Scenario][Expected]` or `test[Method][Condition]` pattern
- Examples: `testExecutePagedSuccess()`, `testExecuteUnpagedWithInvalidParams()`, `testHealthCheckUp()`

**Directory Structure:**
```
src/test/java/
└── com/rmfarma/pharmahub/
    ├── api/resource/
    │   └── QueryExecutionResourceTest.java
    │   └── QueryCatalogResourceTest.java
    │   └── HealthResourceTest.java
    ├── application/
    │   └── ExecuteQueryUseCaseTest.java
    ├── infrastructure/db/
    │   └── JdbcQueryExecutorTest.java
    └── infrastructure/mapper/
        └── TopSellerMapperTest.java
```

## Test Structure

**Quarkus Test Annotation:**

```java
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class QueryExecutionResourceTest {
    
    @Test
    void testExecutePagedSuccess() {
        // Test implementation
    }
    
    @Test
    void testExecuteUnpagedLimitExceeded() {
        // Test implementation
    }
}
```

**Test Method Patterns:**

- Use `@Test` annotation from `org.junit.jupiter.api.Test`
- Method names must start with `test` or use `@Test` (no method name restriction in JUnit 5)
- Use descriptive names indicating scenario and expectation
- No setup/teardown needed for most Quarkus tests (CDI handles injection)

**Setup/Teardown:**

```java
// Per-test setup (if needed)
@BeforeEach
void setUp() {
    // Runs before each test
}

// Per-test cleanup
@AfterEach
void tearDown() {
    // Runs after each test
}

// Suite-level setup
@BeforeAll
static void setUpAll() {
    // Runs once before all tests in class
}

// Suite-level cleanup
@AfterAll
static void tearDownAll() {
    // Runs once after all tests in class
}
```

## REST Endpoint Testing

**Using REST Assured with Quarkus:**

```java
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class QueryExecutionResourceTest {
    
    @Test
    void testExecuteQuerySuccess() {
        given()
            .header("X-API-Key", "d572765238d508028f78d576f0597ccabe0a78958a4ebc02")
            .body("{\"params\": {\"cnpj\": \"06297687000236\"}, \"page\": 1, \"pageSize\": 10}")
            .contentType("application/json")
        .when()
            .post("/queries/top-sellers/execute")
        .then()
            .statusCode(200)
            .body("queryKey", equalTo("top-sellers"))
            .body("mode", equalTo("PAGED"))
            .body("page", equalTo(1))
            .body("items", hasSize(greaterThan(0)));
    }
    
    @Test
    void testExecuteQueryMissingApiKey() {
        given()
            .body("{\"params\": {}}")
            .contentType("application/json")
        .when()
            .post("/queries/top-sellers/execute")
        .then()
            .statusCode(401)
            .body("error", equalTo("UNAUTHORIZED"));
    }
    
    @Test
    void testHealthCheck() {
        given()
        .when()
            .get("/health")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("database", equalTo("connected"));
    }
}
```

## Mocking

**Framework:** Mockito (or Quarkus built-in mocking)
- Not yet configured in `pom.xml`
- Should be added for unit testing application layer and infrastructure

**When to Mock:**
- Database access: Mock `AgroalDataSource` or `QueryRepository`
- External service calls
- Configuration values (can use `@ConfigProperty` injection testing)

**What NOT to Mock:**
- REST endpoint behavior (test against real endpoints with `@QuarkusTest`)
- HTTP headers/status codes
- Response serialization (use actual DTOs/records)
- Logger behavior

**Mock Pattern (Example - After Adding Mockito):**

```java
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class ExecuteQueryUseCaseTest {
    
    @Mock
    private QueryRepository queryRepository;
    
    @Mock
    private QueryExecutor queryExecutor;
    
    @InjectMocks
    private ExecuteQueryUseCase useCase;
    
    @Test
    void testExecuteQueryNotFound() {
        when(queryRepository.findByKey("invalid")).thenReturn(Optional.empty());
        
        assertThrows(QueryNotFoundException.class, () -> {
            useCase.execute("invalid", Map.of(), 1, 10, false);
        });
    }
}
```

## Fixtures and Factories

**Test Data Pattern (When Implementing):**

Create fixture classes to generate consistent test data:

```java
// src/test/java/com/rmfarma/pharmahub/fixtures/QueryDefinitionFixture.java
public class QueryDefinitionFixture {
    
    public static QueryDefinition sampleTopSellersQuery() {
        return new QueryDefinition(
            "top-sellers",
            "1.0",
            "Ranking de vendedores por faturamento",
            "/queries/top-sellers/execute",
            List.of("sales"),
            "SELECT seller, SUM(amount) as total FROM orders GROUP BY seller",
            List.of(
                new ParamDefinition("cnpj", ParamType.STRING, true, null),
                new ParamDefinition("startDate", ParamType.DATE, true, null)
            ),
            10,      // defaultPageSize
            50,      // maxPageSize
            false,   // allowUnpaged
            0,       // maxUnpagedRows
            30000,   // timeoutMs
            "TopSellerDTO",
            "top-sellers"
        );
    }
}

// Usage in test
@Test
void testExecuteTopSellersQuery() {
    QueryDefinition definition = QueryDefinitionFixture.sampleTopSellersQuery();
    // Test with fixture
}
```

**Location:**
- Create under `src/test/java/com/rmfarma/pharmahub/fixtures/`
- Separate test data builders for each aggregate

## Coverage

**Requirements:** Not enforced (no coverage plugin configured in pom.xml)

**Target Areas (Suggested):**
- Application layer (use cases): 80%+
- Infrastructure mappers: 100% (small, critical)
- API resources: 70%+ (complex)
- Exceptions/error paths: 100%

**View Coverage (When Coverage Plugin Added):**

```bash
# Add JaCoCo to pom.xml, then:
./mvnw clean test jacoco:report
# View report at: target/site/jacoco/index.html
```

## Test Types

**Unit Tests:**
- Scope: Single class in isolation
- Target: Application layer (use cases), mappers, utilities
- Approach: Mock dependencies, test business logic
- Example: `ExecuteQueryUseCaseTest` with mocked repositories
- Location: `src/test/java/com/rmfarma/pharmahub/application/`

**Integration Tests:**
- Scope: Multiple layers working together with Quarkus runtime
- Target: REST resources, database integration, filters
- Approach: Use `@QuarkusTest` to load full context
- Run with: `./mvnw test` (JUnit 5 automatically discovers integration tests)
- Example: `QueryExecutionResourceTest` with real endpoints

**E2E Tests:**
- Framework: Not configured
- Could use Testcontainers for database isolation in CI/CD
- Would test full request→database→response flow with real PostgreSQL container

## Common Patterns

**Async Testing (Not Used):**
- Quarkus endpoints are typically synchronous
- If async endpoints added, use `@Test` with `CompletableFuture` or reactive assertions

**Error/Exception Testing:**

```java
import static org.junit.jupiter.api.Assertions.*;

@Test
void testValidationError() {
    ExecuteRequest request = new ExecuteRequest(
        Map.of("wrongParam", "value"),
        1, 10, false
    );
    
    assertThrows(ParamValidationException.class, () -> {
        useCase.execute("top-sellers", request.params(), 1, 10, false);
    });
}

@Test
void testInvalidApiKey() {
    given()
        .header("X-API-Key", "wrong-key")
        .body("{}")
        .contentType("application/json")
    .when()
        .post("/queries/top-sellers/execute")
    .then()
        .statusCode(403)
        .body("error", equalTo("FORBIDDEN"));
}
```

**Response Assertions:**

```java
@Test
void testPagedResponseStructure() {
    given()
        .header("X-API-Key", "d572765238d508028f78d576f0597ccabe0a78958a4ebc02")
        .body("""
            {
              "params": {"cnpj": "06297687000236"},
              "page": 1,
              "pageSize": 10
            }
            """)
        .contentType("application/json")
    .when()
        .post("/queries/top-sellers/execute")
    .then()
        .statusCode(200)
        // Verify paged response structure
        .body("queryKey", notNullValue())
        .body("mode", equalTo("PAGED"))
        .body("page", equalTo(1))
        .body("pageSize", equalTo(10))
        .body("totalItems", greaterThan(0))
        .body("totalPages", greaterThan(0))
        .body("items", hasSize(greaterThan(0)))
        .body("durationMs", greaterThan(0))
        .body("requestId", notNullValue());
}

@Test
void testUnpagedResponseStructure() {
    given()
        .header("X-API-Key", "d572765238d508028f78d576f0597ccabe0a78958a4ebc02")
        .body("""
            {
              "params": {"cnpj": "06297687000236"},
              "unpaged": true
            }
            """)
        .contentType("application/json")
    .when()
        .post("/queries/sales-summary/execute")
    .then()
        .statusCode(200)
        // Verify unpaged response structure
        .body("queryKey", notNullValue())
        .body("mode", equalTo("UNPAGED"))
        .body("returnedItems", greaterThan(0))
        .body("truncated", either(equalTo(true)).or(equalTo(false)))
        .body("items", hasSize(greaterThan(0)))
        .body("durationMs", greaterThan(0))
        .body("requestId", notNullValue());
}
```

## Test Configuration

**Profile-Based Testing:**
- By default, `@QuarkusTest` uses `test` profile
- Test-specific config: `src/test/resources/application-test.properties` (when needed)
- Dev database config already available at `application-dev.properties`

**Database Testing:**
- Tests use configured datasource (currently PostgreSQL)
- Could use H2 in-memory for isolated unit tests: add test dependency for H2 driver
- Testcontainers recommended for realistic CI/CD pipeline testing

---

*Testing analysis: 2026-08-06*
