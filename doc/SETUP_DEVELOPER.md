# 🛠️ Setup do Ambiente de Desenvolvimento — pharma-hub

> Guia para configurar sua máquina e rodar o projeto localmente.

---

## 1. Instalar o SDKMAN (gerenciador de versões Java)

```bash
# Instalar o SDKMAN
curl -s "https://get.sdkman.io" | bash

# Ativar (ou feche e abra o terminal)
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Verificar instalação
sdk version
```

---

## 2. Instalar Java 21

Este projeto usa **Java 21** (declarado no `.sdkmanrc` na raiz).

```bash
# Instalar Java 21 Temurin
sdk install java 21.0.5-tem

# Verificar
java -version
```

### Troca automática de Java por projeto ✅

O arquivo `.sdkmanrc` na raiz do projeto já está configurado. Ative a troca automática uma vez:

```bash
sdk env enable
```

A partir daí, toda vez que entrar na pasta com `cd pharma-hub`, o SDKMAN
troca para Java 21 automaticamente.

---

## 3. Instalar o Google Cloud CLI

```bash
# macOS via Homebrew
brew install google-cloud-sdk

# Verificar
gcloud --version
```

### Autenticar

```bash
# Login da sua conta Google (abre o navegador)
gcloud auth login

# Credenciais para a aplicação (necessário para Secret Manager, Cloud Logging, etc.)
gcloud auth application-default login

# Definir o projeto GCP de desenvolvimento
gcloud config set project rm-farma-dev
```

### Consultar secrets do Secret Manager

```bash
# URL do banco de dev
gcloud secrets versions access latest --secret=pharmahub_db_url --project=rm-farma-dev

# Senha do banco
gcloud secrets versions access latest --secret=pharmahub_db_password --project=rm-farma-dev

# API Key de um cliente
gcloud secrets versions access latest --secret=pharmahub_api_key_pharma_app --project=rm-farma-dev
```

---

## 4. Configurar variáveis de ambiente locais

```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite com seus valores
nano .env   # ou use o editor de sua preferência
```

As variáveis do `.env` são apenas referência. Para dev, as configurações
já estão no `application-dev.properties`. Só é necessário editar se
quiser apontar para um banco diferente do padrão (`localhost:5432`).

---

## 5. Rodar o projeto

```bash
# Modo dev (hot reload + perfil dev automático)
./mvnw quarkus:dev
```

A API sobe em **http://localhost:8080**

---

## 6. Resumo — Comandos do dia a dia

| O que fazer | Comando |
|---|---|
| Ver versão Java ativa | `java -version` |
| Trocar para Java 21 | `sdk use java 21.0.5-tem` |
| Login no GCP | `gcloud auth login` |
| Credenciais ADC (app) | `gcloud auth application-default login` |
| Consultar secret | `gcloud secrets versions access latest --secret=NOME` |
| Rodar em dev | `./mvnw quarkus:dev` |
| Build sem testes | `./mvnw package -DskipTests` |
| Build uber-jar | `./mvnw package -DskipTests -Dquarkus.package.jar.type=uber-jar` |

---

## 7. Checklist do primeiro dia

- [ ] Instalar SDKMAN
- [ ] Instalar Java 21 (`sdk install java 21.0.5-tem`)
- [ ] Instalar gcloud CLI (`brew install google-cloud-sdk`)
- [ ] `gcloud auth login`
- [ ] `gcloud auth application-default login`
- [ ] `gcloud config set project rm-farma-dev`
- [ ] Clonar o repositório
- [ ] `cd pharma-hub` (SDKMAN troca para Java 21 automaticamente)
- [ ] `cp .env.example .env`
- [ ] `./mvnw quarkus:dev`
- [ ] `curl http://localhost:8080/health` → deve retornar `{"status":"UP"}`

---

## 8. Estrutura de ambientes / CI-CD

```
Branch dev  → Cloud Build (cloudbuild-nonprod.yaml) → Cloud Run [rm-farma-dev]
Branch main → Cloud Build (cloudbuild-prod.yaml)    → Cloud Run [rm-farma]
```

**Secrets no GCP Secret Manager** (criados uma vez por projeto):
- `pharmahub_db_url` — URL JDBC do banco
- `pharmahub_db_user` — usuário do banco
- `pharmahub_db_password` — senha do banco
- `pharmahub_api_key_pharma_app` — API Key do cliente pharma-app
- `pharmahub_api_key_admin_dashboard` — API Key do cliente admin-dashboard

