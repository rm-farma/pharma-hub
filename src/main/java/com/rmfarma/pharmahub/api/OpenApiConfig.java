package com.rmfarma.pharmahub.api;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(
        title = "Pharma Hub API",
        version = "1.0.0",
        description = """
                ## Sobre o Pharma Hub

                O **Pharma Hub** é o repositório centralizado de consultas SQL analíticas do Grupo Hiper Saude.
                Ele expõe queries pré-aprovadas via REST, com suporte a paginação, parâmetros tipados e controle de acesso por API Key.

                ## Como usar

                1. Consulte o catálogo de queries disponíveis: `GET /queries`
                2. Veja os parâmetros de uma query específica: `GET /queries/{key}`
                3. Execute a query com seus parâmetros: `POST /queries/{key}/execute`

                ## Autenticação

                Todas as requisições (exceto `GET /health`) exigem o header:
                ```
                X-API-Key: sua-chave-aqui
                ```
                Peça a chave de API ao time de plataforma.

                ## Modos de execução

                | Modo | Quando usar | Campo na requisição |
                |------|-------------|---------------------|
                | **PAGED** | Resultados grandes, com navegação por páginas | `page` + `pageSize` |
                | **UNPAGED** | Queries de resumo (1 linha) ou exportações | `unpaged: true` |

                ## Queries disponíveis

                | Key | Descrição |
                |-----|-----------|
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
                """,
        contact = @Contact(
            name = "Grupo Hiper Saude — Plataforma",
            email = "plataforma@rmfarma.com.br"
        )
    ),
    // Sem "servers" fixo de propósito: sem essa lista, o Swagger UI usa a
    // própria origem de onde a página foi carregada (localhost em dev,
    // a URL real em qualquer Cloud Run) em vez de um host hardcoded.
    // Já foi "http://localhost:8080" fixo e quebrava o "Try it out" em
    // qualquer ambiente implantado (CORS/Failed to fetch) - 2026-08-07.
    tags = {
        @Tag(name = "Health",               description = "Verificação de saúde da aplicação."),
        @Tag(name = "Catálogo de Queries",  description = "Listagem e detalhes das queries disponíveis."),
        @Tag(name = "Execução de Queries",  description = "Execução das queries com parâmetros e paginação.")
    }
)
@SecurityScheme(
    securitySchemeName = "ApiKeyAuth",
    type = SecuritySchemeType.APIKEY,
    apiKeyName = "X-API-Key",
    in = SecuritySchemeIn.HEADER,
    description = "Chave de API enviada no header `X-API-Key`. Peça a chave ao time de plataforma."
)
public class OpenApiConfig extends Application {
}

