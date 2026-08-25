<div align="center">

# 💊 pharma-hub

**Repositório centralizado de consultas SQL analíticas do Grupo Hiper Saúde**

Queries pré-aprovadas de vendas, estoque e curva ABC, expostas via REST — com paginação, parâmetros tipados e autenticação por API Key.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Quarkus](https://img.shields.io/badge/Quarkus-3.27.2-blue?logo=quarkus&logoColor=white)
![BigQuery](https://img.shields.io/badge/BigQuery-Table%20Functions-4285F4?logo=googlebigquery&logoColor=white)
![Cloud Run](https://img.shields.io/badge/Deploy-Cloud%20Run-4285F4?logo=googlecloud&logoColor=white)
![CI](https://github.com/rm-farma/pharma-hub/actions/workflows/ci.yml/badge.svg)
![Status](https://img.shields.io/badge/status-interno-lightgrey)

</div>

---

## 📖 Sobre o projeto

Três endpoints principais:

| Método | Rota | O que faz |
|---|---|---|
| `GET` | `/queries` | Lista o catálogo de queries disponíveis |
| `GET` | `/queries/{key}` | Detalha os parâmetros de uma query |
| `POST` | `/queries/{key}/execute` | Executa a query e retorna o resultado |

Cada query é um par `metadata.yaml` + `query.sql` em `src/main/resources/queries/{query-key}/`. Adicionar uma query nova não exige lógica nova, só esses dois arquivos — detalhes em [`.planning/codebase/STRUCTURE.md`](.planning/codebase/STRUCTURE.md).

<details>
<summary><b>📋 Queries disponíveis</b> (15)</summary>
<br>

`sales-summary` · `sales-overview` · `sales-comparison` · `top-sellers` · `top-products` · `stock-search` · `stock-metrics` · `stock-without-sales` · `idle-stock` · `abc-curve-summary` · `abc-curve-products` · `items-sold-below-cost` · `manufacturer-sales` · `products-loss` · `top-products-by-category`

</details>

## 🧱 Stack

Java 21 · Quarkus 3.27.2 · JAX-RS + Jackson · BigQuery (table functions) · SmallRye OpenAPI/Swagger · GCP Secret Manager · Cloud Run (`southamerica-east1`) · GitHub Actions + Cloud Build

## 🏗️ Arquitetura

Hexagonal, 4 camadas, dependências sempre apontando para o domínio:

```mermaid
flowchart LR
    subgraph API["🌐 api/"]
        A1[Resources, DTOs, ApiKeyFilter]
    end
    subgraph APP["⚙️ application/"]
        A2[Use Cases]
    end
    subgraph CORE["💎 core/"]
        A3["Domínio puro<br/>(models, ports, exceptions)"]
    end
    subgraph INFRA["🔌 infrastructure/"]
        A4["BigQuery, mappers,<br/>config, filesystem"]
    end

    API --> APP --> CORE
    INFRA -. implementa os ports .-> CORE
```

**Requisição típica:** `ApiKeyFilter` valida `X-API-Key` → o use case busca a `QueryDefinition` e valida parâmetros → `BigQueryQueryExecutor` roda a query (uma chamada de table function com parâmetros nomeados `@param`, sem risco de injection) → um `RowMapper` converte o `FieldValueList` retornado.

Cada `query.sql` não é mais um SELECT complexo: é uma única chamada a uma table function do BigQuery (ex.: `` SELECT * FROM `rm-farma-dw-prod.licenciado.get_sales_overview`(@cnpj, @startDate, @endDate) ``) mantida pelo time de dados. O pharma-hub deixou de ser dono da lógica de negócio em SQL — ele só invoca, tipa parâmetros e expõe via REST.

📚 Detalhes completos em [`.planning/codebase/ARCHITECTURE.md`](.planning/codebase/ARCHITECTURE.md).

## ⚙️ Rodando localmente

```bash
# 1. Java 21 via SDKMAN (o projeto já tem .sdkmanrc, troca automática)
curl -s "https://get.sdkman.io" | bash && source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.5-tem

# 2. Autenticação GCP — os DOIS comandos são necessários, servem pra coisas diferentes
gcloud auth login                          # autentica a CLI
gcloud auth application-default login      # gera as ADC, usadas pelo client BigQuery e pra ler o Secret Manager
gcloud config set project rmfarma-dev

# 3. Rodar
./mvnw quarkus:dev
```

Sobe em **http://localhost:8080**. O perfil `dev` usa `quarkus.google.cloud.project-id=rmfarma-dev` como projeto de **execução dos jobs BigQuery (billing)** e de leitura do Secret Manager — **não** é o projeto onde vivem as table functions consultadas. As queries em `src/main/resources/queries/*/query.sql` referenciam a table function pelo nome totalmente qualificado no projeto de dados `rm-farma-dw-prod` (dataset `licenciado`), então é uma consulta cross-project: a identidade autenticada (sua conta via ADC em dev, a service account do Cloud Run em prod) precisa ter `roles/bigquery.dataViewer` no dataset `licenciado` de `rm-farma-dw-prod` e `roles/bigquery.jobUser` em `rmfarma`/`rmfarma-dev`.

> ⚠️ Não usa `.env` nem `env.yaml` — esses arquivos na raiz não são lidos pela aplicação em nenhum perfil hoje.

> 🔑 API Keys de teste (hardcoded em dev): `d572765238d508028f78d576f0597ccabe0a78958a4ebc02` · `test-api-key-456`

<details>
<summary><b>🩹 Erros comuns</b></summary>
<br>

| Erro | Solução |
|---|---|
| `UNAUTHENTICATED: Failed computing credential metadata` | `gcloud auth application-default login` |
| `PERMISSION_DENIED` ao ler secret | Pedir `roles/secretmanager.secretAccessor` em `rmfarma-dev` ao time de infra |
| `PERMISSION_DENIED` / `Access Denied` ao rodar uma query BigQuery | Pedir `roles/bigquery.dataViewer` no dataset `licenciado` de `rm-farma-dw-prod` e `roles/bigquery.jobUser` em `rmfarma-dev` ao time de dados/infra |
| `Header X-API-Key é obrigatório` | Esperado — adicione `-H "X-API-Key: d572765238d508028f78d576f0597ccabe0a78958a4ebc02"` |
| `QueryNotFoundException` | `GET /queries` lista as keys válidas |

</details>

## 🧠 IntelliJ IDEA

- Abrir a raiz do repo — o IntelliJ detecta o `pom.xml` sozinho.
- `Project Structure > Project` → SDK Java 21 (aponte para `~/.sdkman/candidates/java/21.0.5-tem` se não detectar).
- Plugin **"Quarkus Tools"** (opcional) para live reload e navegação nos `application*.properties`.
- Run Configuration Maven com comando `quarkus:dev`.
- Sem Lombok/MapStruct — nenhum processador de anotação extra a configurar.
- Abra o IntelliJ a partir de um terminal já autenticado (`gcloud auth application-default login`) — ele herda as credenciais do shell.

## 🔐 Variáveis de ambiente e secrets

| Perfil | Projeto BigQuery (jobs/billing) | Dados consultados | API Keys |
|---|---|---|---|
| `dev` (local) | `rmfarma-dev` (via ADC) | `rm-farma-dw-prod.licenciado.*` (cross-project) | Hardcoded: `d572765238d508028f78d576f0597ccabe0a78958a4ebc02`, `test-api-key-456` |
| `prod` (Cloud Run) | `rmfarma` (via service account do Cloud Run) | `rm-farma-dw-prod.licenciado.*` (cross-project) | Mesmas hardcoded acima — **⚠️ ver nota abaixo** |

> ⚠️ As chaves `pharma-app`/`admin-dashboard` foram removidas em 2026-08-07 por não terem cliente real associado. Sem chaves específicas de prod, a API em produção passa a aceitar as **mesmas chaves hardcoded do dev** (`d572765238d508028f78d576f0597ccabe0a78958a4ebc02`, `test-api-key-456`) — elas estão públicas no código-fonte. Se o pharma-hub for exposto pra clientes reais, é preciso definir chaves de produção antes disso (ver [Pontos de atenção](#-pontos-de-atenção-conhecidos)).

## 🧪 Testando a API

```bash
curl http://localhost:8080/health                                          # sem auth
curl -H "X-API-Key: d572765238d508028f78d576f0597ccabe0a78958a4ebc02" http://localhost:8080/queries         # catálogo
curl -X POST http://localhost:8080/queries/sales-summary/execute \
  -H "X-API-Key: d572765238d508028f78d576f0597ccabe0a78958a4ebc02" -H "Content-Type: application/json" \
  -d '{"params":{"cnpj":"12345678000100","startDate":"2024-01-01","endDate":"2024-02-01"}}'
```

Ou pelo **Swagger UI** (`/q/swagger-ui`) — clique "Authorize" e cole a API Key uma vez.

## 🌍 Ambientes implantados

Pra times externos consumindo a API — a mesma chave vale nos dois ambientes ([ver secrets](#-variáveis-de-ambiente-e-secrets)):

| Ambiente | URL | Swagger |
|---|---|---|
| 🧪 **Dev** (`rmfarma-dev`) | [`pharma-hub-172688433868.southamerica-east1.run.app`](https://pharma-hub-172688433868.southamerica-east1.run.app) | ✅ [`/q/swagger-ui`](https://pharma-hub-172688433868.southamerica-east1.run.app/q/swagger-ui/) |
| 🔒 **Prod** (`rmfarma`) | [`pharma-hub-575503576839.southamerica-east1.run.app`](https://pharma-hub-575503576839.southamerica-east1.run.app) | ❌ desligado por segurança |

> Sem Swagger disponível em prod? Use a documentação do Dev como referência dos endpoints e troque só a URL base na hora de chamar de verdade.

<details>
<summary><b>💬 Mensagem pronta pra avisar outros times (Teams/Slack)</b></summary>
<br>

```
🔌 Pharma Hub API — Como consumir

Autenticação: toda chamada (exceto /health) exige o header abaixo.
A mesma chave vale para os dois ambientes:

X-API-Key: d572765238d508028f78d576f0597ccabe0a78958a4ebc02

🧪 Dev
https://pharma-hub-172688433868.southamerica-east1.run.app
Documentação interativa (Swagger): /q/swagger-ui

🔒 Prod
https://pharma-hub-575503576839.southamerica-east1.run.app
⚠️ Swagger desativado aqui por segurança — usem a doc do Dev
como referência dos endpoints.

Qualquer dúvida, me chamem.
```

</details>

## 📦 Build

```bash
./mvnw package                                       # target/quarkus-app/quarkus-run.jar
./mvnw package -Dquarkus.package.jar.type=uber-jar   # über-jar (usado no Docker)
```

## 🌿 GitFlow

| Branch | Ambiente (GCP) | Perfil Quarkus | Pipeline |
|---|---|---|---|
| `main` | Produção (`rmfarma`) | `prod` (Swagger desligado) | `cloudbuild-prod.yaml` |
| `develop` | Nonprod (`rmfarma-dev`) | `nonprod` (Swagger ligado) | `cloudbuild-nonprod.yaml` |

> Projeto GCP e perfil Quarkus são coisas diferentes, mas usam o mesmo nome de propósito — `nonprod` existe só pra poder testar via Swagger no ambiente nonprod sem abrir mão da segurança do `prod` de verdade. Ver [`application-nonprod.properties`](src/main/resources/application-nonprod.properties).

```mermaid
gitGraph
    commit id: "v1.0.0" tag: "v1.0.0"
    branch develop
    checkout develop
    commit id: "feature A"
    commit id: "feature B"
    checkout main
    merge develop tag: "v1.1.0"
    checkout develop
    commit id: "feature C"
```

Branches de apoio saem de `develop` (`feature/*`, `release/*`) ou de `main` (`hotfix/*`) e voltam pra lá via PR. `main` só recebe merge de `release/*` ou `hotfix/*`, sempre com tag (`vX.Y.Z`).

O repo já tem a extensão [git-flow](https://github.com/nvie/gitflow) inicializada (`git flow init`). Fluxo recomendado — o PR no GitHub faz o merge de verdade, `git flow finish` só sincroniza local e limpa a branch:

```bash
git flow feature start nome-da-feature
# commits, push, PR no GitHub: feature/nome-da-feature -> develop
# depois do merge do PR:
git flow feature finish nome-da-feature

git flow release start 1.1.0     # a partir de develop
git flow hotfix start bug-x      # a partir de main
```

Sem a extensão instalada, o equivalente é `git checkout -b feature/nome-da-feature` a partir de `develop` — mesmo resultado.

Commits seguem [Conventional Commits](https://www.conventionalcommits.org/): `tipo(escopo): descrição` (`feat`, `fix`, `docs`, `refactor`, `chore`, `test`).

## 🚀 CI/CD

```mermaid
flowchart LR
    D(["push → develop"]) --> CBD["cloudbuild-nonprod.yaml\n(perfil nonprod)"] --> RD["Cloud Run rmfarma-dev\nSwagger em /q/swagger-ui"]
    M(["push → main"]) --> CBM["cloudbuild-prod.yaml\n(perfil prod)"] --> RM["Cloud Run rmfarma\nSwagger desligado"]
```

- **GitHub Actions** (`.github/workflows/ci.yml`): `./mvnw verify` em push/PR pra `main`. Só valida, não deploya.
- **Cloud Build**: builda a imagem e deploya no Cloud Run, um pipeline por ambiente (acima).

<details>
<summary><b>⚠️ Pendências antes do primeiro deploy automático</b> (atualizado em 2026-08-07)</summary>
<br>

- Nenhuma trigger do Cloud Build está conectada ao repo `rm-farma/pharma-hub` — precisa criar as 2 (`develop` → nonprod, `main` → prod).
- ✅ Nomes de secret corrigidos nos dois `cloudbuild-*.yaml` (`gpt-db-host/user/password`, com hífen, iguais nos dois ambientes) — não é mais bloqueio.
- ✅ Permissões da service account do Cloud Build e o Artifact Registry `rm-farma` já estão OK nos dois projetos — não são bloqueio.

</details>

## 📁 Estrutura

```
src/main/java/com/rmfarma/pharmahub/
├── api/              # REST: resources, DTOs, filtros, exception mapper
├── application/      # Use cases
├── core/             # Domínio puro (sem deps externas)
└── infrastructure/   # BigQuery, mappers, config

src/main/resources/
├── application*.properties   # Config base / dev / prod
└── queries/{key}/            # metadata.yaml + query.sql
```

📚 Guia de onde adicionar cada tipo de código em [`.planning/codebase/STRUCTURE.md`](.planning/codebase/STRUCTURE.md).

## ⚠️ Pontos de atenção conhecidos

Auditoria completa em [`.planning/codebase/CONCERNS.md`](.planning/codebase/CONCERNS.md):

- 🔴 **Zero testes** em `src/test/java/`.
- 🔴 Comparação de API Key vulnerável a timing attack (`ApiKeyFilter.java:60`).
- 🟠 Mensagens de erro vazam detalhes internos ao cliente.
- 🟡 **Chave de API única e fixa, compartilhada entre dev e prod** (`d572765238d508028f78d576f0597ccabe0a78958a4ebc02`) — decisão consciente por enquanto (2026-08-07), não um esquecimento. Mitigado pelo `--no-allow-unauthenticated` do Cloud Run (já exige IAM do Google antes de chegar na aplicação). **TODO futuro:** implementar API Keys por cliente via Secret Manager antes de abrir a API pra consumidores externos reais.
- 🟡 `env.yaml` foi removido do controle de versão (2026-08-07), mas já esteve commitado com credenciais em 2 commits antigos — a senha do banco ainda não foi trocada.

## 🔗 Links úteis

[Docs do Quarkus](https://quarkus.io/guides/) · [Quarkus Google Cloud Services (BigQuery)](https://docs.quarkiverse.io/quarkus-google-cloud-services/main/bigquery.html) · [Config YAML](https://quarkus.io/guides/config-yaml) · [SmallRye Health](https://quarkus.io/guides/smallrye-health)

## 🤖 Codando com Claude Code neste projeto

Este repositório já vem instrumentado para desenvolvimento assistido por IA. Antes de pedir pro Claude "entender o projeto do zero", olha se uma dessas ferramentas já não resolve mais rápido e mais barato:

| Ferramenta | O que é | Como usar |
|---|---|---|
| 🦀 **RTK** | Proxy que otimiza tokens de comandos de shell (`git`, `grep`, `ls`...) | Automático via hook — nada a configurar. `rtk gain` mostra a economia acumulada |
| 🧠 **claude-mem** | Memória persistente entre sessões — decisões e descobertas anteriores | Automático. Peça pro Claude buscar contexto antigo (skill `mem-search`) antes de retrabalhar algo |
| 🕸️ **graphify** | Grafo de conhecimento do código (nós/arestas de arquitetura) | `/gsd:graphify build` atualiza · `/gsd:graphify query <termo>` consulta · veja `.planning/graphs/graph.html` |
| 🗂️ **GSD** (*Get Shit Done*) | Framework de planejamento e execução por fases | `/gsd:map-codebase` (já rodado — ver abaixo), `/gsd:plan-phase`, `/gsd:execute-phase` — tudo em `.planning/` |
| 📓 **Obsidian vault** | Camada narrativa de domínio (decisões, conceitos de negócio farmacêutico) | 🚧 Planejado, ainda não implementado |

<details>
<summary><b>📚 Onboarding rápido</b> — o que já está mapeado, sem precisar reler o código inteiro</summary>
<br>

O `/gsd:map-codebase` já gerou uma análise completa do projeto em [`.planning/codebase/`](.planning/codebase/):

- [`ARCHITECTURE.md`](.planning/codebase/ARCHITECTURE.md) · [`STRUCTURE.md`](.planning/codebase/STRUCTURE.md) · [`STACK.md`](.planning/codebase/STACK.md) · [`INTEGRATIONS.md`](.planning/codebase/INTEGRATIONS.md)
- [`CONVENTIONS.md`](.planning/codebase/CONVENTIONS.md) · [`TESTING.md`](.planning/codebase/TESTING.md) · [`CONCERNS.md`](.planning/codebase/CONCERNS.md)

Peça pro Claude ler esses arquivos (ou rodar `/gsd:graphify query <termo>`) antes de sair explorando o código na unha — é mais rápido e mais barato em tokens. Re-rode `/gsd:map-codebase` depois de mudanças grandes de arquitetura para manter isso atualizado.

</details>

<div align="center">

---

Feito com 💊 pelo time **Grupo Hiper Saúde**

</div>
