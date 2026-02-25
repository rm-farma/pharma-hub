# 🚀 Rodando o pharma-hub localmente

## Pré-requisitos

- Java 21 (via SDKMAN — veja `SETUP_DEVELOPER.md`)
- PostgreSQL acessível (local ou via túnel para o Cloud SQL de dev)
- `gcloud` CLI instalado e autenticado

---

## 1. Clonar e configurar o ambiente

```bash
# Entre na pasta do projeto (o SDKMAN troca para Java 21 automaticamente)
cd pharma-hub

# Copie o arquivo de exemplo de variáveis de ambiente
cp .env.example .env
```

Edite o `.env` com os valores reais:

```dotenv
DATABASE_URL=jdbc:postgresql://localhost:5432/pharmahub_dev
DATABASE_USER=postgres
DATABASE_PASSWORD=sua_senha

# API Keys para testar localmente
QUERYHUB_API_KEY_PHARMA_APP=dev-api-key-123
```

> ⚠️ O `.env` **não é lido automaticamente** pelo Quarkus. Use `source .env` antes
> de rodar, ou configure as vars diretamente no `application-dev.properties`.
> Para dev local, as API Keys já estão hardcoded no `application-dev.properties`.

---

## 2. Autenticar no Google Cloud

Necessário caso o projeto use Secret Manager ou Cloud Logging no futuro.

```bash
# Login da conta Google (abre navegador)
gcloud auth login

# Credenciais para a aplicação (ADC — Application Default Credentials)
gcloud auth application-default login

# Definir o projeto GCP de dev
gcloud config set project rm-farma-dev
```

---

## 3. Rodar em modo dev

O Quarkus ativa automaticamente o perfil `dev`, que usa as configurações
do `application-dev.properties` (banco local, log legível, sem JSON).

```bash
./mvnw quarkus:dev
```

A API sobe em: **http://localhost:8080**

---

## 4. Testar os endpoints

```bash
# Health check (não requer API Key)
curl http://localhost:8080/health

# Listar catálogo de queries
curl -H "X-API-Key: dev-api-key-123" http://localhost:8080/queries

# Executar uma query (exemplo: sales-summary)
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

---

## 5. Diferenças entre perfis

| Configuração       | `dev` (local)          | `prod` (Cloud Run)             |
|--------------------|------------------------|--------------------------------|
| Banco              | localhost:5432         | Cloud SQL via SECRET MANAGER   |
| Log formato        | Texto legível          | JSON (Cloud Logging)           |
| Log nível          | DEBUG                  | INFO                           |
| API Keys           | Hardcoded no .properties | Secret Manager via Cloud Build |
| Perfil ativado por | `quarkus:dev` (auto)   | `QUARKUS_PROFILE=prod` (env var) |

---

## 6. Troubleshooting

### Erro: `Unable to acquire JDBC Connection`
Verifique se o PostgreSQL está rodando e se as credenciais no
`application-dev.properties` estão corretas.

### Erro: `401 Unauthorized`
Certifique-se de enviar o header `X-API-Key: dev-api-key-123` nas requisições.

### Erro: `QueryNotFoundException`
A query key não existe no catálogo. Use `GET /queries` para listar as
queries disponíveis.

