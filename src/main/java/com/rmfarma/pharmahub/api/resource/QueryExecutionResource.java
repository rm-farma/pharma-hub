package com.rmfarma.pharmahub.api.resource;

import com.rmfarma.pharmahub.api.dto.request.ExecuteRequest;
import com.rmfarma.pharmahub.api.dto.response.PagedResponse;
import com.rmfarma.pharmahub.api.dto.response.UnpagedResponse;
import com.rmfarma.pharmahub.application.ExecuteQueryUseCase;
import com.rmfarma.pharmahub.core.model.ExecutionMode;
import com.rmfarma.pharmahub.core.model.PagedResult;
import com.rmfarma.pharmahub.core.model.UnpagedResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;

@Path("/queries/{key}/execute")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Execução de Queries", description = "Executa consultas SQL registradas no catálogo, com suporte a paginação e modo sem paginação.")
@SecurityRequirement(name = "ApiKeyAuth")
public class QueryExecutionResource {

    private static final Logger LOG = Logger.getLogger(QueryExecutionResource.class);

    @Inject
    ExecuteQueryUseCase executeQueryUseCase;

    @POST
    @Operation(
        summary = "Executar uma query do catálogo",
        description = """
                Executa uma query SQL registrada identificada por `key`.

                ## Modos de execução

                ### Paginado (padrão)
                Retorna uma página de resultados. Use `page` e `pageSize` para navegar.
                A resposta inclui `totalItems` e `totalPages` para construção de paginação no frontend.

                ### Sem paginação (`unpaged: true`)
                Retorna todos os registros até o limite `maxUnpagedRows` definido na query.
                Se o limite for excedido, `truncated: true` e `truncatedMessage` informam o corte.
                Útil para exportações ou queries que retornam naturalmente poucos registros (ex: `sales-summary`).

                ## Parâmetros da query
                Envie os parâmetros no campo `params` como um objeto JSON.
                Os tipos são convertidos automaticamente (`DATE`, `INTEGER`, `DECIMAL`, etc.).
                Use `GET /queries/{key}` para consultar os parâmetros exigidos por cada query.

                ## Autenticação
                Todas as requisições exigem o header `X-API-Key` com uma chave válida.
                O `clientId` resolvido é injetado internamente e registrado nos logs.
                """
    )
    @Parameter(
        name = "key",
        description = "Identificador da query a executar. Deve existir no catálogo (`GET /queries`).",
        required = true,
        example = "top-sellers"
    )
    @RequestBody(
        description = """
                Estrutura do request — todos os campos disponíveis:

                | Campo      | Tipo      | Default  | Descrição                                                                    |
                |------------|-----------|----------|------------------------------------------------------------------------------|
                | `params`   | `object`  | —        | Parâmetros da query. Variam por query — consulte os exemplos abaixo          |
                | `page`     | `integer` | `1`      | Número da página, **começa em 1**. Ignorado quando `unpaged: true`           |
                | `pageSize` | `integer` | *varies* | Itens por página. Máximo e default variam por query. Ignorado se `unpaged`   |
                | `unpaged`  | `boolean` | `false`  | Se `true`, retorna todos os registros até `maxUnpagedRows` sem paginação     |

                > **Dica:** use `GET /queries/{key}` para ver os parâmetros exigidos, defaults e limites de cada query.
                """,
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = {
                @ExampleObject(
                    name = "01 · sales-summary (unpaged)",
                    summary = "sales-summary — Total faturado e total de pedidos. Sempre retorna 1 linha.",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01"
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                ),
                @ExampleObject(
                    name = "02 · sales-overview (unpaged)",
                    summary = "sales-overview — Faturamento, CMV e total de pedidos. Sempre retorna 1 linha.",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01"
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                ),
                @ExampleObject(
                    name = "03 · sales-comparison (unpaged)",
                    summary = "sales-comparison — Comparativo de dois períodos com variação % de faturamento, itens e ticket médio. Retorna 1 linha.",
                    value = """
                            {
                              "params": {
                                "cnpj":       "12345678000100",
                                "startDate1": "2025-01-01",
                                "endDate1":   "2025-02-01",
                                "startDate2": "2024-01-01",
                                "endDate2":   "2024-02-01"
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                ),
                @ExampleObject(
                    name = "04 · top-sellers (paginado, página 1)",
                    summary = "top-sellers — Ranking de vendedores por faturamento. Paginado. default=10, max pageSize=50.",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01",
                                "limit":     10
                              },
                              "page":     1,
                              "pageSize": 10,
                              "unpaged":  false
                            }
                            """
                ),
                @ExampleObject(
                    name = "04 · top-sellers (paginado, página 2)",
                    summary = "top-sellers — Como buscar a página 2: incremente page mantendo os mesmos params e pageSize.",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01",
                                "limit":     10
                              },
                              "page":     2,
                              "pageSize": 10,
                              "unpaged":  false
                            }
                            """
                ),
                @ExampleObject(
                    name = "04 · top-sellers (unpaged — exportar tudo)",
                    summary = "top-sellers — Modo sem paginação: retorna todos os vendedores de uma vez (até 500 registros).",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01",
                                "limit":     500
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                ),
                @ExampleObject(
                    name = "05 · top-products (paginado)",
                    summary = "top-products — Ranking de produtos por quantidade vendida. Paginado. default=10, max pageSize=100.",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01",
                                "limit":     10
                              },
                              "page":     1,
                              "pageSize": 10,
                              "unpaged":  false
                            }
                            """
                ),
                @ExampleObject(
                    name = "05 · top-products (unpaged — exportar tudo)",
                    summary = "top-products — Sem paginação: retorna todos os produtos de uma vez (até 1000 registros).",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01",
                                "limit":     1000
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                ),
                @ExampleObject(
                    name = "06 · stock-search (paginado)",
                    summary = "stock-search — Busca por EAN ou nome (ILIKE). O % deve ser enviado pelo cliente. Não suporta unpaged. default=20, max pageSize=100.",
                    value = """
                            {
                              "params": {
                                "cnpj":       "12345678000100",
                                "searchTerm": "%dipirona%"
                              },
                              "page":     1,
                              "pageSize": 20,
                              "unpaged":  false
                            }
                            """
                ),
                @ExampleObject(
                    name = "07 · stock-metrics (unpaged)",
                    summary = "stock-metrics — Custo total do estoque e itens de alta rotatividade. Sempre retorna 1 linha.",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100"
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                ),
                @ExampleObject(
                    name = "08 · stock-without-sales (paginado)",
                    summary = "stock-without-sales — Produtos em estoque que nunca tiveram venda. default=20, max pageSize=100.",
                    value = """
                            {
                              "params": {
                                "cnpj":  "12345678000100",
                                "limit": 20
                              },
                              "page":     1,
                              "pageSize": 20,
                              "unpaged":  false
                            }
                            """
                ),
                @ExampleObject(
                    name = "08 · stock-without-sales (unpaged — exportar tudo)",
                    summary = "stock-without-sales — Sem paginação: retorna todos os produtos parados de uma vez (até 5000 registros).",
                    value = """
                            {
                              "params": {
                                "cnpj":  "12345678000100",
                                "limit": 5000
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                ),
                @ExampleObject(
                    name = "09 · idle-stock (paginado)",
                    summary = "idle-stock — Estoque parado com totais agregados (totalSkus, totalUnidades, valorTotalCusto, valorTotalVenda). default=20, max pageSize=100.",
                    value = """
                            {
                              "params": {
                                "cnpj":  "12345678000100",
                                "limit": 20
                              },
                              "page":     1,
                              "pageSize": 20,
                              "unpaged":  false
                            }
                            """
                ),
                @ExampleObject(
                    name = "09 · idle-stock (unpaged — exportar tudo)",
                    summary = "idle-stock — Sem paginação: retorna todo o estoque parado de uma vez (até 5000 registros). Os campos de totais se repetem em cada linha.",
                    value = """
                            {
                              "params": {
                                "cnpj":  "12345678000100",
                                "limit": 5000
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                ),
                @ExampleObject(
                    name = "10 · abc-curve-summary (unpaged)",
                    summary = "abc-curve-summary — Totais de produtos e faturamento por classe A/B/C. Sempre retorna 1 linha.",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01"
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                ),
                @ExampleObject(
                    name = "11 · abc-curve-products (paginado, filtrando classe A)",
                    summary = "abc-curve-products — Produtos com classe ABC, faturamento, estoque e preços. classeAbc é opcional (A, B ou C). default=50, max pageSize=200.",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01",
                                "classeAbc": "A"
                              },
                              "page":     1,
                              "pageSize": 50,
                              "unpaged":  false
                            }
                            """
                ),
                @ExampleObject(
                    name = "11 · abc-curve-products (unpaged — todas as classes)",
                    summary = "abc-curve-products — Sem filtro de classe e sem paginação: retorna todos os produtos classificados A/B/C (até 10.000 registros).",
                    value = """
                            {
                              "params": {
                                "cnpj":      "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate":   "2025-02-01",
                                "classeAbc": null
                              },
                              "page":     null,
                              "pageSize": null,
                              "unpaged":  true
                            }
                            """
                )
            }
        )
    )
    @APIResponse(
        responseCode = "200",
        description = "Query executada com sucesso. Retorna `PagedResponse` (paginado) ou `UnpagedResponse` (`unpaged: true`).",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = {
                @ExampleObject(
                    name = "200 · PAGED — top-sellers (página 1 de 4)",
                    summary = "Resposta paginada: queryKey, mode, page, pageSize, totalItems, totalPages, items, durationMs, requestId",
                    value = """
                            {
                              "queryKey":   "top-sellers",
                              "mode":       "PAGED",
                              "page":       1,
                              "pageSize":   10,
                              "totalItems": 34,
                              "totalPages": 4,
                              "items": [
                                { "seller": "João Silva",  "totalAmount": 128450.90, "totalOrders": 312 },
                                { "seller": "Maria Souza", "totalAmount":  97320.50, "totalOrders": 245 },
                                { "seller": "Carlos Lima", "totalAmount":  84100.00, "totalOrders": 198 }
                              ],
                              "durationMs": 42,
                              "requestId":  "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                            }
                            """
                ),
                @ExampleObject(
                    name = "200 · UNPAGED — sales-summary (1 linha)",
                    summary = "Resposta unpaged: queryKey, mode, returnedItems, truncated, truncatedMessage, items, durationMs, requestId",
                    value = """
                            {
                              "queryKey":         "sales-summary",
                              "mode":             "UNPAGED",
                              "returnedItems":    1,
                              "truncated":        false,
                              "truncatedMessage": null,
                              "items": [
                                { "totalAmount": 589430.75, "totalOrders": 1842 }
                              ],
                              "durationMs": 18,
                              "requestId":  "b2c3d4e5-f6a7-8901-bcde-f12345678901"
                            }
                            """
                ),
                @ExampleObject(
                    name = "200 · UNPAGED — sales-overview (1 linha com CMV)",
                    summary = "Resposta sales-overview: totalAmount, cmv e totalOrders",
                    value = """
                            {
                              "queryKey":         "sales-overview",
                              "mode":             "UNPAGED",
                              "returnedItems":    1,
                              "truncated":        false,
                              "truncatedMessage": null,
                              "items": [
                                { "totalAmount": 589430.75, "cmv": 312840.50, "totalOrders": 1842 }
                              ],
                              "durationMs": 22,
                              "requestId":  "c1d2e3f4-a5b6-7890-cdef-123456789012"
                            }
                            """
                ),
                @ExampleObject(
                    name = "200 · UNPAGED — sales-comparison (1 linha com variações %)",
                    summary = "Resposta sales-comparison: dois períodos com faturamento, itens e ticket médio e variação % de cada",
                    value = """
                            {
                              "queryKey":         "sales-comparison",
                              "mode":             "UNPAGED",
                              "returnedItems":    1,
                              "truncated":        false,
                              "truncatedMessage": null,
                              "items": [
                                {
                                  "periodoBase":            "2025-01-01",
                                  "periodoComparado":       "2024-01-01",
                                  "faturamentoBase":        589430.75,
                                  "faturamentoComparado":   512300.00,
                                  "variacaoFaturamento":    15.07,
                                  "itensVendidosBase":      4821,
                                  "itensVendidosComparado": 4102,
                                  "variacaoItensVendidos":  17.53,
                                  "ticketMedioBase":        320.15,
                                  "ticketMedioComparado":   287.40,
                                  "variacaoTicketMedio":    11.39
                                }
                              ],
                              "durationMs": 35,
                              "requestId":  "d4e5f6a7-b8c9-0123-defa-234567890123"
                            }
                            """
                ),
                @ExampleObject(
                    name = "200 · UNPAGED — abc-curve-summary (1 linha com totais A/B/C)",
                    summary = "Resposta abc-curve-summary: totais de produtos e faturamento por classe",
                    value = """
                            {
                              "queryKey":         "abc-curve-summary",
                              "mode":             "UNPAGED",
                              "returnedItems":    1,
                              "truncated":        false,
                              "truncatedMessage": null,
                              "items": [
                                {
                                  "totalProdutos":  842,
                                  "totalProdutosA": 98,
                                  "totalProdutosB": 186,
                                  "totalProdutosC": 558,
                                  "faturamentoTotal": 589430.75,
                                  "faturamentoA":    471544.60,
                                  "faturamentoB":     88414.61,
                                  "faturamentoC":     29471.54
                                }
                              ],
                              "durationMs": 95,
                              "requestId":  "e5f6a7b8-c9d0-1234-efab-345678901234"
                            }
                            """
                ),
                @ExampleObject(
                    name = "200 · PAGED — idle-stock (com totais agregados em cada linha)",
                    summary = "Resposta idle-stock: cada linha inclui dados do produto E os totais agregados (totalSkus, totalUnidades, valorTotalCusto, valorTotalVenda)",
                    value = """
                            {
                              "queryKey":   "idle-stock",
                              "mode":       "PAGED",
                              "page":       1,
                              "pageSize":   20,
                              "totalItems": 87,
                              "totalPages": 5,
                              "items": [
                                {
                                  "ean":            "7896714200046",
                                  "apresentacao":   "VITAMINA C 1G 30CPR",
                                  "fabricante":     "EMS",
                                  "grupoMacro":     "VITAMINAS",
                                  "saldoEstoque":   240,
                                  "custoMedioTotal": 1680.00,
                                  "precoVenda":     12.90,
                                  "totalSkus":      "87",
                                  "totalUnidades":  "4321",
                                  "valorTotalCusto": "38420.50",
                                  "valorTotalVenda": "71840.90"
                                }
                              ],
                              "durationMs": 78,
                              "requestId":  "f6a7b8c9-d0e1-2345-fabc-456789012345"
                            }
                            """
                ),
                @ExampleObject(
                    name = "200 · UNPAGED truncado — abc-curve-products (limite atingido)",
                    summary = "Quando o número de registros excede maxUnpagedRows: truncated=true e truncatedMessage informa o corte. Use paginação para evitar truncamento.",
                    value = """
                            {
                              "queryKey":         "abc-curve-products",
                              "mode":             "UNPAGED",
                              "returnedItems":    10000,
                              "truncated":        true,
                              "truncatedMessage": "Resultado truncado. Máximo de 10000 linhas retornadas.",
                              "items": [
                                {
                                  "ean":                  "7896714200046",
                                  "apresentacao":         "DIPIRONA SODICA 500MG 20CPR",
                                  "faturamentoTotal":     48320.50,
                                  "quantidadeVendida":    9210,
                                  "numTransacoes":        412,
                                  "percentualIndividual": 8.19,
                                  "percentualAcumulado":  8.19,
                                  "classeAbc":            "A",
                                  "saldoEstoque":         340,
                                  "precoVenda":           12.90,
                                  "custoMedio":            7.20
                                }
                              ],
                              "durationMs": 310,
                              "requestId":  "a9b8c7d6-e5f4-3210-abcd-098765432100"
                            }
                            """
                )
            }
        )
    )
    @APIResponse(
        responseCode = "401",
        description = "Header `X-API-Key` ausente.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                value = """
                        {
                          "error": "Unauthorized",
                          "message": "X-API-Key header ausente",
                          "details": null,
                          "requestId": null
                        }
                        """
            )
        )
    )
    @APIResponse(
        responseCode = "403",
        description = "X-API-Key inválida ou não autorizada.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                value = """
                        {
                          "error": "Forbidden",
                          "message": "API Key inválida",
                          "details": null,
                          "requestId": null
                        }
                        """
            )
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Query não encontrada no catálogo.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                value = """
                        {
                          "error": "QueryNotFoundException",
                          "message": "Query não encontrada: minha-query-inexistente",
                          "details": null,
                          "requestId": "d4e5f6a7-b8c9-0123-defa-234567890123"
                        }
                        """
            )
        )
    )
    @APIResponse(
        responseCode = "422",
        description = "Parâmetro obrigatório ausente ou com tipo inválido.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                value = """
                        {
                          "error": "ParamValidationException",
                          "message": "Parâmetro obrigatório ausente: cnpj",
                          "details": null,
                          "requestId": "e5f6a7b8-c9d0-1234-efab-345678901234"
                        }
                        """
            )
        )
    )
    @APIResponse(
        responseCode = "500",
        description = "Erro interno inesperado.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                value = """
                        {
                          "error": "InternalServerError",
                          "message": "Erro inesperado ao executar a query",
                          "details": "could not execute query",
                          "requestId": "f6a7b8c9-d0e1-2345-fabc-456789012345"
                        }
                        """
            )
        )
    )
    public Response execute(@PathParam("key") String key, ExecuteRequest request,
                            @Context HttpHeaders headers) {
        String requestId = UUID.randomUUID().toString();
        String clientId = headers.getHeaderString("X-Client-Id");
        long start = System.currentTimeMillis();

        Map<String, Object> params = request != null && request.params() != null
                ? request.params() : Map.of();

        ExecuteQueryUseCase.ExecutionResult result = executeQueryUseCase.execute(
                key, params, request != null ? request.page() : null,
                request != null ? request.pageSize() : null,
                request != null ? request.unpaged() : null
        );

        long durationMs = System.currentTimeMillis() - start;

        Object responseBody;
        int rowsReturned;

        if (result.mode() == ExecutionMode.PAGED) {
            PagedResult<?> paged = (PagedResult<?>) result.result();
            rowsReturned = paged.items().size();
            responseBody = new PagedResponse<>(
                    key, "PAGED", paged.page(), paged.pageSize(),
                    paged.totalItems(), paged.totalPages(), paged.items(), durationMs, requestId
            );
        } else {
            UnpagedResult<?> unpaged = (UnpagedResult<?>) result.result();
            rowsReturned = unpaged.returnedItems();
            String truncatedMessage = unpaged.truncated()
                    ? "Resultado truncado. Máximo de %d linhas retornadas.".formatted(result.definition().maxUnpagedRows())
                    : null;
            responseBody = new UnpagedResponse<>(
                    key, "UNPAGED", unpaged.returnedItems(), unpaged.truncated(),
                    truncatedMessage, unpaged.items(), durationMs, requestId
            );
        }

        LOG.infov("Query executada: requestId={0}, clientId={1}, queryKey={2}, mode={3}, durationMs={4}, rowsReturned={5}, status=OK",
                requestId, clientId, key, result.mode(), durationMs, rowsReturned);

        return Response.ok(responseBody).build();
    }
}


