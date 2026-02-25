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
        description = "Parâmetros da query e opções de paginação.",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = {
                @ExampleObject(
                    name = "sales-summary — resumo de vendas (unpaged)",
                    summary = "sales-summary: total faturado e pedidos por CNPJ/período",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate": "2025-02-01"
                              },
                              "unpaged": true
                            }
                            """
                ),
                @ExampleObject(
                    name = "sales-overview — visão geral com CMV (unpaged)",
                    summary = "sales-overview: faturamento, CMV e pedidos",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate": "2025-02-01"
                              },
                              "unpaged": true
                            }
                            """
                ),
                @ExampleObject(
                    name = "top-sellers — top vendedores paginado",
                    summary = "top-sellers: ranking de vendedores por faturamento",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate": "2025-02-01",
                                "limit": 10
                              },
                              "page": 1,
                              "pageSize": 10
                            }
                            """
                ),
                @ExampleObject(
                    name = "top-products — top produtos paginado",
                    summary = "top-products: ranking de produtos por quantidade vendida",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate": "2025-02-01",
                                "limit": 20
                              },
                              "page": 1,
                              "pageSize": 20
                            }
                            """
                ),
                @ExampleObject(
                    name = "sales-comparison — comparativo de dois períodos (unpaged)",
                    summary = "sales-comparison: variação % de faturamento, itens e ticket médio",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "startDate1": "2025-01-01",
                                "endDate1":   "2025-02-01",
                                "startDate2": "2024-01-01",
                                "endDate2":   "2024-02-01"
                              },
                              "unpaged": true
                            }
                            """
                ),
                @ExampleObject(
                    name = "stock-search — busca de produto no estoque",
                    summary = "stock-search: busca por EAN ou nome (ILIKE)",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "searchTerm": "%dipirona%"
                              },
                              "page": 1,
                              "pageSize": 20
                            }
                            """
                ),
                @ExampleObject(
                    name = "stock-metrics — métricas gerais do estoque (unpaged)",
                    summary = "stock-metrics: custo total e itens de alta rotatividade",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100"
                              },
                              "unpaged": true
                            }
                            """
                ),
                @ExampleObject(
                    name = "stock-without-sales — estoque sem venda (paginado)",
                    summary = "stock-without-sales: produtos em estoque que nunca tiveram venda",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "limit": 20
                              },
                              "page": 1,
                              "pageSize": 20
                            }
                            """
                ),
                @ExampleObject(
                    name = "idle-stock — estoque parado com resumo (paginado)",
                    summary = "idle-stock: produtos parados com custo total e valor de venda",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "limit": 20
                              },
                              "page": 1,
                              "pageSize": 20
                            }
                            """
                ),
                @ExampleObject(
                    name = "abc-curve-summary — resumo curva ABC (unpaged)",
                    summary = "abc-curve-summary: total de produtos e faturamento por classe A/B/C",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate": "2025-02-01"
                              },
                              "unpaged": true
                            }
                            """
                ),
                @ExampleObject(
                    name = "abc-curve-products — detalhamento curva ABC (paginado)",
                    summary = "abc-curve-products: produtos com classe, faturamento, estoque e preços",
                    value = """
                            {
                              "params": {
                                "cnpj": "12345678000100",
                                "startDate": "2025-01-01",
                                "endDate": "2025-02-01",
                                "classeAbc": "A"
                              },
                              "page": 1,
                              "pageSize": 50
                            }
                            """
                )
            }
        )
    )
    @APIResponse(
        responseCode = "200",
        description = "Query executada com sucesso. Retorna `PagedResponse` ou `UnpagedResponse` conforme o modo.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = {
                @ExampleObject(
                    name = "Resposta paginada (top-sellers)",
                    summary = "Exemplo de resposta paginada",
                    value = """
                            {
                              "queryKey": "top-sellers",
                              "mode": "PAGED",
                              "page": 1,
                              "pageSize": 10,
                              "totalItems": 34,
                              "totalPages": 4,
                              "items": [
                                { "seller": "João Silva",  "totalAmount": 128450.90, "totalOrders": 312 },
                                { "seller": "Maria Souza", "totalAmount": 97320.50,  "totalOrders": 245 }
                              ],
                              "durationMs": 42,
                              "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                            }
                            """
                ),
                @ExampleObject(
                    name = "Resposta unpaged (sales-summary)",
                    summary = "Exemplo de resposta sem paginação",
                    value = """
                            {
                              "queryKey": "sales-summary",
                              "mode": "UNPAGED",
                              "returnedItems": 1,
                              "truncated": false,
                              "truncatedMessage": null,
                              "items": [
                                { "totalAmount": 589430.75, "totalOrders": 1842 }
                              ],
                              "durationMs": 18,
                              "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678901"
                            }
                            """
                ),
                @ExampleObject(
                    name = "Resposta truncada (unpaged com muitos dados)",
                    summary = "Exemplo de resposta com truncamento ativo",
                    value = """
                            {
                              "queryKey": "abc-curve-products",
                              "mode": "UNPAGED",
                              "returnedItems": 10000,
                              "truncated": true,
                              "truncatedMessage": "Resultado truncado. Máximo de 10000 linhas retornadas.",
                              "items": [ "..." ],
                              "durationMs": 310,
                              "requestId": "c3d4e5f6-a7b8-9012-cdef-123456789012"
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


