# Graph Report - pharma-hub  (2026-08-07)

## Corpus Check

- 81 files · ~22,760 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary

- 464 nodes · 733 edges · 36 communities (31 shown, 5 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 17 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness

- Built from commit: `4917509c`
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
- Testing Patterns
- Coding Conventions
- Codebase Concerns
- External Integrations
- TopSellerMapper.java
- Technology Stack
- Codebase Structure

## God Nodes (most connected - your core abstractions)
1. `ResultSetMapper` - 31 edges
2. `QueryDefinition` - 28 edges
3. `Communities (28 total, 5 thin omitted)` - 24 edges
4. `JdbcQueryExecutor` - 18 edges
5. `FileSystemQueryRepository` - 15 edges
6. `💊 pharma-hub` - 15 edges
7. `QueryRepository` - 14 edges
8. `ExecuteQueryUseCase` - 13 edges
9. `ParamType` - 12 edges
10. `Architecture` - 12 edges

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

## Communities (36 total, 5 thin omitted)

### Community 0 - "QueryDefinition"
Cohesion: 0.11
Nodes (17): ExecuteQueryUseCase, ExecutionResult, ApplicationScoped, Logger, GetQueryDetailsUseCase, ApplicationScoped,
ApplicationScoped, ListQueriesUseCase (+9 more)

### Community 1 - "JdbcQueryExecutor"

Cohesion: 0.11
Nodes (22): FunctionalInterface, Instance, AgroalDataSource, ApplicationScoped, Logger, Override, Pattern,
SuppressWarnings (+14 more)

### Community 2 - "Override"
Cohesion: 0.13
Nodes (12): bind(), fromString(), Override, PreparedStatement, ParamType, BOOLEAN, DATE, DECIMAL (+4 more)

### Community 3 - "GlobalExceptionMapper.java"

Cohesion: 0.13
Nodes (11): ExceptionMapper, ErrorResponse, GlobalExceptionMapper, Logger, Override, Provider, Response,
MaxRowsExceededException (+3 more)

### Community 4 - "ResultSetMapper"

Cohesion: 0.36
Nodes (6): SalesOverviewDTO, ApplicationScoped, Named, Override, ResultSet, SalesOverviewMapper

### Community 5 - "QueryExecutionResource.java"

Cohesion: 0.13
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

Cohesion: 0.06
Nodes (34): Communities (28 total, 5 thin omitted), Community 0 - "QueryDefinition", Community 10 - "🚀 Rodando o
pharma-hub localmente", Community 11 - "HealthResource.java", Community 12 - "pharma-hub", Community 13 - "
AbcCurveProductMapper.java", Community 14 - "AbcCurveSummaryMapper.java", Community 15 - "IdleStockMapper.java" (+26
more)

### Community 10 - "🚀 Rodando o pharma-hub localmente"

Cohesion: 0.11
Nodes (18): Anti-Patterns, Architectural Constraints, Architecture, Component Responsibilities, Cross-Cutting Concerns,
Data Flow, Duplicated Mapper Classes for Each Query, Entry Points (+10 more)

### Community 11 - "HealthResource.java"
Cohesion: 0.33
Nodes (9): HealthResource, AgroalDataSource, APIResponse, GET, Operation, Path, Produces, Response (+1 more)

### Community 12 - "pharma-hub"

Cohesion: 0.13
Nodes (15): 🏗️ Arquitetura, 📦 Build, 🚀 CI/CD, 🤖 Codando com Claude Code neste projeto, 📁 Estrutura, 🌿 GitFlow, 🧠
IntelliJ IDEA, 🔗 Links úteis (+7 more)

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

### Community 28 - "Testing Patterns"

Cohesion: 0.18
Nodes (11): Common Patterns, Coverage, Fixtures and Factories, Mocking, REST Endpoint Testing, Test Configuration, Test
File Organization, Test Framework (+3 more)

### Community 29 - "Coding Conventions"

Cohesion: 0.20
Nodes (10): Code Style, Coding Conventions, Comments, Error Handling, Function Design, Import Organization, Logging,
Module Design (+2 more)

### Community 30 - "Codebase Concerns"

Cohesion: 0.22
Nodes (9): Architectural Concerns, Codebase Concerns, Dependencies at Risk, Fragile Areas, Missing Critical Features,
Performance Bottlenecks, Security Considerations, Tech Debt (+1 more)

### Community 31 - "External Integrations"

Cohesion: 0.22
Nodes (9): API Endpoints, APIs & External Services, Authentication & Identity, CI/CD & Deployment, Data Storage,
Environment Configuration, External Integrations, Monitoring & Observability (+1 more)

### Community 32 - "TopSellerMapper.java"

Cohesion: 0.36
Nodes (6): TopSellerDTO, ApplicationScoped, Named, Override, ResultSet, TopSellerMapper

### Community 33 - "Technology Stack"

Cohesion: 0.25
Nodes (7): Configuration, Frameworks, Key Dependencies, Languages, Platform Requirements, Runtime, Technology Stack

### Community 34 - "Codebase Structure"

Cohesion: 0.25
Nodes (7): Codebase Structure, Directory Layout, Directory Purposes, Key File Locations, Naming Conventions, Special
Directories, Where to Add New Code

## Knowledge Gaps

- **112 isolated node(s):** `com.rmfarma:pharma-hub`, `PAGED`, `UNPAGED`, `PaginationRequest`, `System Overview` (+107
  more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ResultSetMapper` connect `JdbcQueryExecutor`
  to `TopSellerMapper.java`, `ResultSetMapper`, `AbcCurveProductMapper.java`, `AbcCurveSummaryMapper.java`,
  `IdleStockMapper.java`, `SalesComparisonMapper.java`, `SalesSummaryMapper.java`, `StockMetricsMapper.java`,
  `StockSearchMapper.java`, `StockWithoutSalesMapper.java`, `TopProductMapper.java`?**
  _High betweenness centrality (0.220) - this node is a cross-community bridge._
- **Why does `QueryDefinition` connect `QueryDefinition` to `JdbcQueryExecutor`, `FileSystemQueryRepository`, `QueryCatalogResource.java`?**
  _High betweenness centrality (0.137) - this node is a cross-community bridge._
- **Why does `QueryExecutor` connect `QueryDefinition` to `JdbcQueryExecutor`?**
  _High betweenness centrality (0.052) - this node is a cross-community bridge._
- **What connects `com.rmfarma:pharma-hub`, `PAGED`, `UNPAGED` to the rest of the system?**
  _112 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `QueryDefinition` be split into smaller, more focused modules?**
  _Cohesion score 0.10668563300142248 - nodes in this community are weakly interconnected._
- **Should `JdbcQueryExecutor` be split into smaller, more focused modules?**
  _Cohesion score 0.11095305832147938 - nodes in this community are weakly interconnected._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.12962962962962962 - nodes in this community are weakly interconnected._