# Graph Report - pharma-hub  (2026-08-06)

## Corpus Check
- 75 files · ~12,116 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 370 nodes · 638 edges · 28 communities (23 shown, 5 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 17 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d5a12857`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- QueryDefinition
- JdbcQueryExecutor
- Override
- GlobalExceptionMapper.java
- ResultSetMapper
- QueryExecutionResource.java
- FileSystemQueryRepository
- QueryCatalogResource.java
- ApiKeyFilter
- 🛠️ Setup do Ambiente de Desenvolvimento — pharma-hub
- 🚀 Rodando o pharma-hub localmente
- HealthResource.java
- pharma-hub
- AbcCurveProductMapper.java
- AbcCurveSummaryMapper.java
- IdleStockMapper.java
- SalesComparisonMapper.java
- SalesSummaryMapper.java
- StockMetricsMapper.java
- StockSearchMapper.java
- StockWithoutSalesMapper.java
- TopProductMapper.java
- MavenWrapperDownloader
- QueryHubConfig
- OpenApiConfig.java
- mvnw
- PaginationRequest.java
- com.rmfarma:pharma-hub

## God Nodes (most connected - your core abstractions)
1. `ResultSetMapper` - 31 edges
2. `QueryDefinition` - 28 edges
3. `JdbcQueryExecutor` - 18 edges
4. `FileSystemQueryRepository` - 15 edges
5. `QueryRepository` - 14 edges
6. `ExecuteQueryUseCase` - 13 edges
7. `ParamType` - 12 edges
8. `QueryCatalogResource` - 9 edges
9. `QueryExecutionResource` - 9 edges
10. `NamedParamResolver` - 9 edges

## Surprising Connections (you probably didn't know these)
- `QueryCatalogResource` --references--> `GetQueryDetailsUseCase`  [EXTRACTED]
  src/main/java/com/rmfarma/pharmahub/api/resource/QueryCatalogResource.java → src/main/java/com/rmfarma/pharmahub/application/GetQueryDetailsUseCase.java
- `QueryCatalogResource` --references--> `ListQueriesUseCase`  [EXTRACTED]
  src/main/java/com/rmfarma/pharmahub/api/resource/QueryCatalogResource.java → src/main/java/com/rmfarma/pharmahub/application/ListQueriesUseCase.java
- `QueryExecutionResource` --references--> `ExecuteQueryUseCase`  [EXTRACTED]
  src/main/java/com/rmfarma/pharmahub/api/resource/QueryExecutionResource.java → src/main/java/com/rmfarma/pharmahub/application/ExecuteQueryUseCase.java
- `ParamDefinition` --references--> `ParamType`  [EXTRACTED]
  src/main/java/com/rmfarma/pharmahub/core/model/ParamDefinition.java → src/main/java/com/rmfarma/pharmahub/core/model/ParamType.java
- `QueryDefinition` --references--> `ParamDefinition`  [EXTRACTED]
  src/main/java/com/rmfarma/pharmahub/core/model/QueryDefinition.java → src/main/java/com/rmfarma/pharmahub/core/model/ParamDefinition.java

## Import Cycles
- None detected.

## Communities (28 total, 5 thin omitted)

### Community 0 - "QueryDefinition"
Cohesion: 0.11
Nodes (16): ExecuteQueryUseCase, ExecutionResult, ApplicationScoped, Logger, GetQueryDetailsUseCase, ApplicationScoped, ApplicationScoped, ListQueriesUseCase (+8 more)

### Community 1 - "JdbcQueryExecutor"
Cohesion: 0.13
Nodes (19): Instance, AgroalDataSource, ApplicationScoped, Logger, Override, Pattern, SuppressWarnings, JdbcQueryExecutor (+11 more)

### Community 2 - "Override"
Cohesion: 0.13
Nodes (12): bind(), fromString(), Override, PreparedStatement, ParamType, BOOLEAN, DATE, DECIMAL (+4 more)

### Community 3 - "GlobalExceptionMapper.java"
Cohesion: 0.11
Nodes (12): ExceptionMapper, ErrorResponse, GlobalExceptionMapper, Logger, Override, Provider, Response, MaxRowsExceededException (+4 more)

### Community 4 - "ResultSetMapper"
Cohesion: 0.14
Nodes (15): FunctionalInterface, SalesOverviewDTO, TopSellerDTO, ApplicationScoped, Named, Override, ResultSet, SalesOverviewMapper (+7 more)

### Community 5 - "QueryExecutionResource.java"
Cohesion: 0.14
Nodes (17): Consumes, HttpHeaders, POST, RequestBody, ExecuteRequest, PagedResponse, UnpagedResponse, APIResponse (+9 more)

### Community 6 - "FileSystemQueryRepository"
Cohesion: 0.17
Nodes (7): PostConstruct, ParamDefinition, FileSystemQueryRepository, ApplicationScoped, Logger, Override, SuppressWarnings

### Community 7 - "QueryCatalogResource.java"
Cohesion: 0.24
Nodes (12): PaginationInfo, ParamInfo, QueryInfoResponse, APIResponse, GET, Operation, Parameter, Path (+4 more)

### Community 8 - "ApiKeyFilter"
Cohesion: 0.24
Nodes (9): ContainerRequestContext, ContainerRequestFilter, PreMatching, ApiKeyFilter, Logger, Override, Provider, ApiKeyConfig (+1 more)

### Community 9 - "🛠️ Setup do Ambiente de Desenvolvimento — pharma-hub"
Cohesion: 0.15
Nodes (12): 1. Instalar o SDKMAN (gerenciador de versões Java), 2. Instalar Java 21, 3. Instalar o Google Cloud CLI, 4. Configurar variáveis de ambiente locais, 5. Rodar o projeto, 6. Resumo — Comandos do dia a dia, 7. Checklist do primeiro dia, 8. Estrutura de ambientes / CI-CD (+4 more)

### Community 10 - "🚀 Rodando o pharma-hub localmente"
Cohesion: 0.17
Nodes (11): 1. Clonar e configurar o ambiente, 2. Autenticar no Google Cloud, 3. Rodar em modo dev, 4. Testar os endpoints, 5. Diferenças entre perfis, 6. Troubleshooting, Erro: `401 Unauthorized`, Erro: `QueryNotFoundException` (+3 more)

### Community 11 - "HealthResource.java"
Cohesion: 0.33
Nodes (9): HealthResource, AgroalDataSource, APIResponse, GET, Operation, Path, Produces, Response (+1 more)

### Community 12 - "pharma-hub"
Cohesion: 0.20
Nodes (9): Creating a native executable, Packaging and running the application, pharma-hub, Provided Code, Related Guides, REST, Running the application in dev mode, SmallRye Health (+1 more)

### Community 13 - "AbcCurveProductMapper.java"
Cohesion: 0.36
Nodes (6): AbcCurveProductDTO, AbcCurveProductMapper, ApplicationScoped, Named, Override, ResultSet

### Community 14 - "AbcCurveSummaryMapper.java"
Cohesion: 0.36
Nodes (6): AbcCurveSummaryDTO, AbcCurveSummaryMapper, ApplicationScoped, Named, Override, ResultSet

### Community 15 - "IdleStockMapper.java"
Cohesion: 0.36
Nodes (6): IdleStockDTO, IdleStockMapper, ApplicationScoped, Named, Override, ResultSet

### Community 16 - "SalesComparisonMapper.java"
Cohesion: 0.36
Nodes (6): SalesComparisonDTO, ApplicationScoped, Named, Override, ResultSet, SalesComparisonMapper

### Community 17 - "SalesSummaryMapper.java"
Cohesion: 0.36
Nodes (6): SalesSummaryDTO, ApplicationScoped, Named, Override, ResultSet, SalesSummaryMapper

### Community 18 - "StockMetricsMapper.java"
Cohesion: 0.36
Nodes (6): StockMetricsDTO, ApplicationScoped, Named, Override, ResultSet, StockMetricsMapper

### Community 19 - "StockSearchMapper.java"
Cohesion: 0.36
Nodes (6): StockSearchDTO, ApplicationScoped, Named, Override, ResultSet, StockSearchMapper

### Community 20 - "StockWithoutSalesMapper.java"
Cohesion: 0.36
Nodes (6): StockWithoutSalesDTO, ApplicationScoped, Named, Override, ResultSet, StockWithoutSalesMapper

### Community 21 - "TopProductMapper.java"
Cohesion: 0.36
Nodes (6): TopProductDTO, ApplicationScoped, Named, Override, ResultSet, TopProductMapper

### Community 24 - "OpenApiConfig.java"
Cohesion: 0.70
Nodes (4): Application, OpenAPIDefinition, SecurityScheme, OpenApiConfig

## Knowledge Gaps
- **29 isolated node(s):** `com.rmfarma:pharma-hub`, `PAGED`, `UNPAGED`, `PaginationRequest`, `Running the application in dev mode` (+24 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ResultSetMapper` connect `ResultSetMapper` to `JdbcQueryExecutor`, `AbcCurveProductMapper.java`, `AbcCurveSummaryMapper.java`, `IdleStockMapper.java`, `SalesComparisonMapper.java`, `SalesSummaryMapper.java`, `StockMetricsMapper.java`, `StockSearchMapper.java`, `StockWithoutSalesMapper.java`, `TopProductMapper.java`?**
  _High betweenness centrality (0.346) - this node is a cross-community bridge._
- **Why does `QueryDefinition` connect `QueryDefinition` to `JdbcQueryExecutor`, `FileSystemQueryRepository`, `QueryCatalogResource.java`?**
  _High betweenness centrality (0.217) - this node is a cross-community bridge._
- **Why does `QueryExecutor` connect `QueryDefinition` to `JdbcQueryExecutor`?**
  _High betweenness centrality (0.081) - this node is a cross-community bridge._
- **What connects `com.rmfarma:pharma-hub`, `PAGED`, `UNPAGED` to the rest of the system?**
  _29 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `QueryDefinition` be split into smaller, more focused modules?**
  _Cohesion score 0.10960960960960961 - nodes in this community are weakly interconnected._
- **Should `JdbcQueryExecutor` be split into smaller, more focused modules?**
  _Cohesion score 0.1268939393939394 - nodes in this community are weakly interconnected._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.12962962962962962 - nodes in this community are weakly interconnected._