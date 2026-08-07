# pharma-hub

Repositório centralizado de consultas SQL analíticas do **Grupo Hiper Saúde**. Expõe queries pré-aprovadas (vendas, estoque, curva ABC) via REST, com paginação, parâmetros tipados e controle de acesso por API Key.

Construído com [Quarkus](https://quarkus.io/) (Java 21), publicado no Google Cloud Run.

---

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Stack tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Queries disponíveis](#queries-disponíveis)
- [Pré-requisitos](#pré-requisitos)
- [Setup do ambiente de desenvolvimento](#setup-do-ambiente-de-desenvolvimento)
- [Configurando o IntelliJ IDEA](#configurando-o-intellij-idea)
- [Rodando localmente](#rodando-localmente)
- [Variáveis de ambiente e secrets](#variáveis-de-ambiente-e-secrets)
- [Testando a API](#testando-a-api)
- [Build e empacotamento](#build-e-empacotamento)
- [GitFlow — fluxo de trabalho com Git](#gitflow--fluxo-de-trabalho-com-git)
- [CI/CD e Deploy](#cicd-e-deploy)
- [Estrutura de diretórios](#estrutura-de-diretórios)
- [Pontos de atenção conhecidos](#pontos-de-atenção-conhecidos)
- [Checklist do primeiro dia](#checklist-do-primeiro-dia)
- [Links úteis](#links-úteis)

---

## Sobre o projeto

O Pharma Hub abstrai um conjunto de queries analíticas em PostgreSQL atrás de uma API REST simples:

1. `GET /queries` — lista o catálogo de queries disponíveis
2. `GET /queries/{key}` — detalha parâmetros de uma query específica
3. `POST /queries/{key}/execute` — executa a query com os parâmetros informados

Cada query é definida declarativamente por um par `metadata.yaml` + `query.sql` em `src/main/resources/queries/{query-key}/`, carregado uma vez na inicialização da aplicação. Adicionar uma nova query não exige lógica nova — só os arquivos de definição (veja [Estrutura de diretórios](#estrutura-de-diretórios)).

## Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem / Runtime | Java 21 (Eclipse Temurin) |
| Framework | Quarkus 3.27.2 |
| API | JAX-RS (`quarkus-rest`), serialização Jackson |
| Banco de dados | PostgreSQL via JDBC (`quarkus-jdbc-postgresql` + Agroal) |
| Documentação de API | SmallRye OpenAPI + Swagger UI |
| Config | `quarkus-config-yaml`, SnakeYAML (metadados das queries) |
| Segredos | GCP Secret Manager (`quarkus-google-cloud-secret-manager`) |
| Observabilidade | Micrometer + Prometheus, SmallRye Health, `quarkus-logging-json` |
| Build | Maven (`./mvnw`) |
| Empacotamento | Über-jar → imagem Docker (`eclipse-temurin:21-jre-alpine`) |
| Deploy | Google Cloud Run, região `southamerica-east1` |
| CI | GitHub Actions (build/verify) + Google Cloud Build (imagem + deploy) |

## Arquitetura

Arquitetura hexagonal (Ports & Adapters) em 4 camadas, com inversão de dependência apontando para o domínio:

```
api/  (REST, DTOs, filtros, exception mapper)
  └──> application/  (use cases: orquestração)
         └──> core/  (domínio puro: models, ports, exceptions — zero dependências externas)
                ▲
infrastructure/ (JDBC, mappers, config, filesystem) ── implementa os ports do core
```

**Fluxo de uma requisição** (`POST /queries/{key}/execute`):

1. `ApiKeyFilter` valida o header `X-API-Key` (exceto em `/health` e `/q/*`)
2. `ExecuteQueryUseCase` busca a `QueryDefinition`, valida/resolve parâmetros e decide o modo (`PAGED`/`UNPAGED`)
3. `JdbcQueryExecutor` monta e executa o SQL (parâmetros nomeados `:param` viram `?` via `NamedParamResolver`, com binding tipado — sem risco de SQL injection)
4. Um `ResultSetMapper` específico (ou o `GenericMapMapper` genérico) converte as linhas do `ResultSet`
5. `GlobalExceptionMapper` centraliza a tradução de exceções de domínio para status HTTP

Detalhamento completo (diagramas, responsabilidades por classe, anti-patterns conhecidos) em [`.planning/codebase/ARCHITECTURE.md`](.planning/codebase/ARCHITECTURE.md).

## Queries disponíveis

| Key | Descrição |
|---|---|
| `sales-summary` | Total faturado e pedidos por período |
| `sales-overview` | Faturamento, CMV e pedidos |
| `sales-comparison` | Comparativo de dois períodos com variação % |
| `top-sellers` | Ranking de vendedores por faturamento |
| `top-products` | Ranking de produtos por quantidade vendida |
| `stock-search` | Busca de produto por EAN ou nome |
| `stock-metrics` | Métricas gerais do estoque |
| `stock-without-sales` | Produtos em estoque sem registro de venda |
| `idle-stock` | Estoque parado com custo e valor total |
| `abc-curve-summary` | Resumo da curva ABC por classe A/B/C |
| `abc-curve-products` | Detalhamento de produtos na curva ABC |

## Pré-requisitos

- **Java 21** (recomendado via [SDKMAN](https://sdkman.io/) — o projeto já tem `.sdkmanrc`)
- **gcloud CLI** instalado e com acesso ao projeto GCP `rmfarma-dev`
- **Maven** — não precisa instalar, o wrapper `./mvnw` já vem no repo
- Conta Google com permissão de leitura no Secret Manager do projeto `rmfarma-dev` (peça acesso ao time de infra se necessário)

## Setup do ambiente de desenvolvimento

### 1. Instalar o SDKMAN e o Java 21

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk install java 21.0.5-tem
sdk env enable   # ativa a troca automática de versão ao entrar na pasta do projeto
```

A partir daí, todo `cd pharma-hub` já ativa o Java 21 automaticamente (via `.sdkmanrc`).

### 2. Instalar e autenticar o gcloud CLI

```bash
brew install google-cloud-sdk   # macOS

gcloud auth login                          # autentica sua conta Google na CLI
gcloud auth application-default login      # gera as ADC — credenciais que a APLICAÇÃO usa
gcloud config set project rmfarma-dev
```

> ⚠️ **Os dois primeiros comandos fazem coisas diferentes.** `gcloud auth login` autentica só a CLI para uso manual. `gcloud auth application-default login` gera as *Application Default Credentials* (ADC) — são essas que o Quarkus usa para falar com o Secret Manager em `quarkus:dev`. **Sem o segundo comando, a aplicação não sobe localmente** (erro `UNAUTHENTICATED: Failed computing credential metadata`).

### 3. Clonar o repositório

```bash
git clone https://github.com/rm-farma/pharma-hub.git
cd pharma-hub   # SDKMAN já troca pro Java 21 automaticamente
```

## Configurando o IntelliJ IDEA

1. **Abrir o projeto:** `File > Open` e selecione a raiz do repositório (o IntelliJ detecta o `pom.xml` automaticamente).
2. **JDK do projeto:** `File > Project Structure > Project` → defina o SDK para **Java 21** (Temurin). Se o IntelliJ não listar automaticamente a instalação do SDKMAN, aponte manualmente para `~/.sdkman/candidates/java/21.0.5-tem`.
3. **Plugin do Quarkus (opcional, recomendado):** `Settings > Plugins` → instale **"Quarkus Tools"** — dá suporte a live reload, navegação nos `application*.properties` e execução via UI.
4. **Importar como projeto Maven:** o IntelliJ já reconhece o `pom.xml`; não é necessário nenhum plugin de anotação/Lombok — o projeto não usa Lombok nem MapStruct.
5. **Run Configuration para modo dev:**
   - `Run > Edit Configurations > + > Maven`
   - **Command line:** `quarkus:dev`
   - **Working directory:** raiz do projeto
   - Isso reproduz exatamente `./mvnw quarkus:dev`, com hot reload.
6. **Credenciais GCP:** o IntelliJ herda as variáveis de ambiente do shell que o abriu. Se você autenticou via `gcloud auth application-default login` no terminal (passo anterior) **antes** de abrir a IDE, funciona sem configuração extra. Se rodar a aplicação pela IDE e cair no erro de autenticação, feche e reabra o IntelliJ a partir de um terminal já autenticado.
7. **Encoding:** o projeto usa UTF-8 (`project.build.sourceEncoding=UTF-8`). Confira em `Settings > Editor > File Encodings` que tudo está como UTF-8 — alguns arquivos `.properties` legados do repo estão em Latin-1 (ver [Pontos de atenção conhecidos](#pontos-de-atenção-conhecidos)), então preste atenção ao editá-los.

## Rodando localmente

Com o Java 21 ativo e as ADC configuradas (passos acima):

```bash
./mvnw quarkus:dev
```

O perfil `dev` (`application-dev.properties`) ativa automaticamente e:
- Busca a URL/usuário/senha do banco **direto do GCP Secret Manager** do projeto `rmfarma-dev` (secrets `gpt-db-host`, `gpt-db-user`, `gpt-db-password`) — **não** depende de nenhum `.env` ou `env.yaml` local.
- Usa API Keys hardcoded para teste: `dev-api-key-123` (cliente `dev-client`) e `test-api-key-456` (cliente `test-client`).
- Log em texto legível, nível `DEBUG` para o pacote `com.rmfarma`.

A API sobe em **http://localhost:8080**. Dev UI do Quarkus disponível em `http://localhost:8080/q/dev/`.

### Troubleshooting

| Erro | Causa provável | Solução |
|---|---|---|
| `UNAUTHENTICATED: Failed computing credential metadata` | ADC não configuradas | `gcloud auth application-default login` |
| `PERMISSION_DENIED` ao ler secret | Sua conta não tem `roles/secretmanager.secretAccessor` em `rmfarma-dev` | Peça acesso ao time de infra |
| `{"error":"UNAUTHORIZED","message":"Header X-API-Key é obrigatório"}` | Requisição sem o header `X-API-Key` | Adicione `-H "X-API-Key: dev-api-key-123"` — isso é comportamento esperado, não é bug |
| `Unable to acquire JDBC Connection` | Sem rede até o host do banco, ou secret desatualizado | Confirme que consegue alcançar o host resolvido em `gpt-db-host` |
| `QueryNotFoundException` | Key de query não existe no catálogo | `GET /queries` lista as keys válidas |

## Variáveis de ambiente e secrets

**Importante:** os arquivos `.env`, `.env.example`, `env.yaml` e `env.yaml.example` na raiz **não são lidos pela aplicação em nenhum perfil hoje** — são referência histórica/manual, não estão plugados no `application-dev.properties` nem no `application-prod.properties`. Não perca tempo editando-os esperando efeito em `quarkus:dev`.

### Perfil `dev` (local)

| Origem | Valor |
|---|---|
| `quarkus.datasource.jdbc.url/username/password` | Resolvidos via `${sm//<secret>}` do GCP Secret Manager, projeto `rmfarma-dev` |
| Secrets usados | `gpt-db-host`, `gpt-db-user`, `gpt-db-password` |
| API Keys | Hardcoded em `application-dev.properties` / `application.properties` (`dev-api-key-123`, `test-api-key-456`) |
| Autenticação necessária | `gcloud auth application-default login` (uma vez por máquina) |

### Perfil `prod` (Cloud Run)

| Env var | Injetada por | Secret Manager (`rmfarma`) |
|---|---|---|
| `DATABASE_URL` | Cloud Build `--set-secrets` | `pharmahub_db_url` |
| `DATABASE_USER` | Cloud Build `--set-secrets` | `pharmahub_db_user` |
| `DATABASE_PASSWORD` | Cloud Build `--set-secrets` | `pharmahub_db_password` |
| `API_KEY_PHARMA_APP` | Cloud Build `--set-secrets` | `pharmahub_api_key_pharma_app` |
| `API_KEY_ADMIN_DASHBOARD` | Cloud Build `--set-secrets` | `pharmahub_api_key_admin_dashboard` |
| `QUARKUS_PROFILE` | Cloud Build `--set-env-vars` | `prod` (fixo) |
| `LOG_LEVEL`, `LOG_JSON`, `GCP_LOGGING_ENABLED` | Cloud Build `--set-env-vars` | ver `cloudbuild-prod.yaml` |

> ⚠️ Auditoria em 2026-08-06 confirmou que **esses 5 secrets de prod ainda não existem** no projeto `rmfarma` no Secret Manager — precisam ser criados antes do primeiro deploy funcionar. Veja [CI/CD e Deploy](#cicd-e-deploy).

## Testando a API

Via `curl`:

```bash
# Health check — não exige API Key
curl http://localhost:8080/health

# Listar catálogo de queries
curl -H "X-API-Key: dev-api-key-123" http://localhost:8080/queries

# Executar uma query
curl -X POST http://localhost:8080/queries/sales-summary/execute \
  -H "X-API-Key: dev-api-key-123" \
  -H "Content-Type: application/json" \
  -d '{
    "params": {
      "cnpj": "12345678000100",
      "startDate": "2024-01-01",
      "endDate": "2024-02-01"
    }
  }'
```

Ou via **Swagger UI** (`http://localhost:8080/q/swagger-ui`) — clique em "Authorize" e cole `dev-api-key-123` uma vez para autenticar todas as chamadas de teste feitas pela interface. Desabilitado em produção.

## Build e empacotamento

```bash
./mvnw package                                          # gera target/quarkus-app/quarkus-run.jar
java -jar target/quarkus-app/quarkus-run.jar

./mvnw package -Dquarkus.package.jar.type=uber-jar       # über-jar (usado no Docker/Cloud Run)
java -jar target/*-runner.jar

./mvnw package -Dnative                                  # executável nativo (requer GraalVM)
./mvnw package -Dnative -Dquarkus.native.container-build=true   # nativo via container, sem GraalVM local
```

## GitFlow — fluxo de trabalho com Git

Este projeto adota o modelo **[GitFlow](https://nvie.com/posts/a-successful-git-branching-model/)** clássico. Duas branches permanentes, mapeadas 1:1 nos ambientes de deploy:

| Branch | Papel | Ambiente | Pipeline |
|---|---|---|---|
| `main` | Produção — sempre estável, sempre deployável | Cloud Run prod (`rmfarma`) | `cloudbuild-prod.yaml` |
| `develop` | Integração — próxima release em construção | Cloud Run nonprod (`rmfarma-dev`) | `cloudbuild-nonprod.yaml` |

Branches de apoio, sempre criadas a partir de `develop` (ou `main`, no caso de hotfix) e descartadas após o merge:

```
feature/<slug>    a partir de develop  →  volta para develop
release/<versão>  a partir de develop  →  vai para main (tag) E volta para develop
hotfix/<slug>     a partir de main     →  vai para main (tag) E volta para develop
```

```
main     ─●──────────────────●───────────────●────────►  (tags v1.0.0, v1.1.0, hotfix v1.1.1)
           \                /  \             /
develop     ●──●──●──●──●──●    ●──●──●──●──●──────────►
              \    /                  \
feature/x      ●──●                    (release/1.1.0)
```

### Ferramenta `git-flow` (opcional)

O repositório já está inicializado com a extensão [git-flow](https://github.com/nvie/gitflow) (`git flow init`), que dá comandos de atalho para criar/finalizar as branches de apoio. Config já salva em `.git/config`:

| Config | Valor |
|---|---|
| Branch de produção | `main` |
| Branch de desenvolvimento | `develop` |
| Prefixo de feature | `feature/` |
| Prefixo de release | `release/` |
| Prefixo de hotfix | `hotfix/` |
| Prefixo de support | `support/` |
| Prefixo de tag de versão | *(nenhum — tags ficam `v1.0.0`, não `versionv1.0.0`)* |

Instalação (uma vez por máquina, não é obrigatória para trabalhar no projeto):

```bash
brew install git-flow-avh   # ou git-flow (nvie), qualquer um dos dois clientes funciona
```

Se preferir não instalar, os comandos equivalentes em git puro estão na segunda aba abaixo — dão exatamente o mesmo resultado.

### Comandos do dia a dia

**Com a extensão `git-flow`:**

```bash
# Nova feature
git flow feature start nome-da-feature       # cria feature/nome-da-feature a partir de develop
# ... commits ...
git push -u origin feature/nome-da-feature
# Abrir PR no GitHub: feature/nome-da-feature -> develop
# Após o merge do PR:
git flow feature finish nome-da-feature      # limpa a branch local (já foi mergeada via PR)

# Preparar uma release
git flow release start 1.1.0                 # cria release/1.1.0 a partir de develop
# ajustes finais, bump de versão, sem features novas
git push -u origin release/1.1.0
# PR: release/1.1.0 -> main (após aprovação)
git flow release finish 1.1.0                # cria a tag v1.1.0 em main e mergeia de volta em develop

# Hotfix urgente em produção
git flow hotfix start corrige-bug-critico    # cria hotfix/corrige-bug-critico a partir de main
# ... fix ...
git push -u origin hotfix/corrige-bug-critico
# PR: hotfix/... -> main
git flow hotfix finish corrige-bug-critico   # cria a tag em main e mergeia de volta em develop
```

> ⚠️ **`git flow *  finish` faz merge local automaticamente**, sem passar por PR/code review. Se o merge já foi feito via PR no GitHub (fluxo recomendado, acima), o `finish` serve só para sincronizar `develop`/`main` locais e apagar a branch de apoio — rode-o só depois que o PR já tiver sido mergeado remotamente, senão ele duplica o merge localmente.

**Sem a extensão (git puro — resultado idêntico):**

```bash
# Nova feature
git checkout develop
git pull
git checkout -b feature/nome-da-feature
# ... commits ...
git push -u origin feature/nome-da-feature
# Abrir PR: feature/nome-da-feature -> develop

# Preparar uma release
git checkout develop
git checkout -b release/1.1.0
# ajustes finais, bump de versão, sem features novas
# PR: release/1.1.0 -> main (após aprovação, mergear também de volta em develop)

# Hotfix urgente em produção
git checkout main
git checkout -b hotfix/corrige-bug-critico
# ... fix ...
# PR: hotfix/... -> main (mergear também de volta em develop)
```

### Convenção de commits

O repositório já segue [Conventional Commits](https://www.conventionalcommits.org/) — mantenha o padrão:

```
<tipo>(<escopo opcional>): <descrição curta no imperativo>

tipos usados no projeto: feat, fix, docs, refactor, chore, test
```

Exemplos reais do histórico: `fix(adapter-db): corrige cálculo de página para consultas paginadas`, `docs(QueryExecutionResource): atualiza documentação de parâmetros de consulta`.

### Regras práticas

- Nunca commitar direto em `main` ou `develop` — sempre via branch + PR.
- `main` só recebe merge de `release/*` ou `hotfix/*`.
- Toda `release/*` e todo `hotfix/*` termina com uma tag em `main` (`vMAJOR.MINOR.PATCH`) e é mergeado de volta em `develop` para não perder o fix.
- PR para `main` exige que o pipeline de CI (`.github/workflows/ci.yml`) esteja verde.

## CI/CD e Deploy

Duas ferramentas, dois papéis:

- **GitHub Actions** (`.github/workflows/ci.yml`) — roda `./mvnw verify` em push/PR para `main`. Validação rápida, não builda imagem nem faz deploy.
- **Google Cloud Build** — builda a imagem Docker e faz deploy no Cloud Run. Dois pipelines, um por ambiente:
  - `cloudbuild-nonprod.yaml` → branch `develop` → Cloud Run em `rmfarma-dev`
  - `cloudbuild-prod.yaml` → branch `main` → Cloud Run em `rmfarma`

### Estado atual (auditado em 2026-08-06) — pendências antes do primeiro deploy automático

- [ ] **Nenhuma trigger do Cloud Build está conectada ao repo `rm-farma/pharma-hub`** em nenhum dos dois projetos GCP. Precisa criar as 2 triggers (uma por branch).
- [ ] `cloudbuild-nonprod.yaml` referencia os secrets `gpt_db_host`/`gpt_db_user`/`gpt_db_password` (com underscore); os secrets reais em `rmfarma-dev` usam hífen: `gpt-db-host`/`gpt-db-user`/`gpt-db-password`. Precisa corrigir o nome.
- [ ] `cloudbuild-nonprod.yaml` não injeta nenhuma API Key — `application-prod.properties` exige `API_KEY_PHARMA_APP`/`API_KEY_ADMIN_DASHBOARD` sem default, então o boot falha sem isso.
- [ ] Os 5 secrets referenciados em `cloudbuild-prod.yaml` (`pharmahub_db_url`, `pharmahub_db_user`, `pharmahub_db_password`, `pharmahub_api_key_pharma_app`, `pharmahub_api_key_admin_dashboard`) **não existem** no projeto `rmfarma` — precisam ser criados no Secret Manager.
- Permissões da service account do Cloud Build já estão OK nos dois projetos (`run.admin`, `secretmanager.secretAccessor`, `iam.serviceAccountUser`) — não é um bloqueio.
- Artifact Registry `rm-farma` (`southamerica-east1`) já existe nos dois projetos — não é um bloqueio.

## Estrutura de diretórios

```
pharma-hub/
├── src/main/java/com/rmfarma/pharmahub/
│   ├── api/              # REST: resources, DTOs, filtros, exception mapper
│   ├── application/      # Use cases (orquestração de negócio)
│   ├── core/             # Domínio puro: models, ports, exceptions (zero deps externas)
│   └── infrastructure/   # JDBC, mappers, config, repositório em filesystem
├── src/main/resources/
│   ├── application*.properties   # Config base / dev / prod
│   └── queries/{key}/            # metadata.yaml + query.sql, um dir por query
├── src/test/java/                # Testes (hoje vazio — ver Pontos de atenção)
├── .planning/codebase/            # Mapeamento estrutural gerado (GSD) — arquitetura, stack, convenções
├── .planning/graphs/               # Grafo de conhecimento do código (graphify)
├── cloudbuild-nonprod.yaml         # Pipeline de deploy — branch develop
├── cloudbuild-prod.yaml            # Pipeline de deploy — branch main
└── Dockerfile                      # Imagem de runtime (JRE Alpine)
```

Guia completo de onde adicionar cada tipo de código novo (nova query, novo use case, novo endpoint) em [`.planning/codebase/STRUCTURE.md`](.planning/codebase/STRUCTURE.md).

## Pontos de atenção conhecidos

Auditoria completa em [`.planning/codebase/CONCERNS.md`](.planning/codebase/CONCERNS.md). Destaques:

- **Zero testes** — `src/test/java/` está vazio. Qualquer refatoração hoje é sem rede de segurança.
- **Comparação de API Key vulnerável a timing attack** (`ApiKeyFilter.java:60`, usa `.equals()` em vez de comparação em tempo constante).
- **Mensagens de erro vazam detalhes internos** ao cliente (`GlobalExceptionMapper.java:42`, `HealthResource.java:83`).
- **`application-dev.properties` e `application-prod.properties` estão em encoding Latin-1**, não UTF-8 — cuidado ao editar (acentos podem corromper silenciosamente).
- **`env.yaml` está no `.gitignore` mas hoje aparece modificado no `git status`**, sugerindo que já foi commitado com credenciais em algum momento do histórico — vale investigar (`git log -p -- env.yaml`) e trocar a senha do banco se confirmado.

## Checklist do primeiro dia

- [ ] Instalar SDKMAN + Java 21 (`sdk install java 21.0.5-tem`)
- [ ] Instalar gcloud CLI
- [ ] `gcloud auth login`
- [ ] `gcloud auth application-default login`
- [ ] `gcloud config set project rmfarma-dev`
- [ ] Clonar o repositório e abrir no IntelliJ (ver [Configurando o IntelliJ IDEA](#configurando-o-intellij-idea))
- [ ] `./mvnw quarkus:dev`
- [ ] `curl http://localhost:8080/health` → deve retornar `{"status":"UP"}`
- [ ] `curl -H "X-API-Key: dev-api-key-123" http://localhost:8080/queries` → deve listar as 11 queries
- [ ] Ler o [GitFlow](#gitflow--fluxo-de-trabalho-com-git) antes do primeiro PR

## Links úteis

- [Documentação do Quarkus](https://quarkus.io/guides/)
- [Micrometer + Prometheus](https://quarkus.io/guides/micrometer)
- [REST Jackson](https://quarkus.io/guides/rest#json-serialisation)
- [YAML Config](https://quarkus.io/guides/config-yaml)
- [Datasources (Agroal)](https://quarkus.io/guides/datasource)
- [Logging JSON](https://quarkus.io/guides/logging#json-logging)
- [SmallRye Health](https://quarkus.io/guides/smallrye-health)
