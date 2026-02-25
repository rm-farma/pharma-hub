package com.rmfarma.pharmahub.api.resource;

import com.rmfarma.pharmahub.api.dto.response.QueryInfoResponse;
import com.rmfarma.pharmahub.application.GetQueryDetailsUseCase;
import com.rmfarma.pharmahub.application.ListQueriesUseCase;
import com.rmfarma.pharmahub.core.model.QueryDefinition;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/queries")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Catálogo de Queries", description = "Listagem e detalhes das queries SQL disponíveis no Pharma Hub.")
@SecurityRequirement(name = "ApiKeyAuth")
public class QueryCatalogResource {

    @Inject
    ListQueriesUseCase listQueriesUseCase;

    @Inject
    GetQueryDetailsUseCase getQueryDetailsUseCase;

    @GET
    @Operation(
        summary = "Listar todas as queries disponíveis",
        description = """
                Retorna o catálogo completo de queries SQL registradas no Pharma Hub.
                Cada entrada contém metadados da query: chave, descrição, parâmetros aceitos e configurações de paginação.

                Use o campo `key` retornado aqui para executar a query via `POST /queries/{key}/execute`.
                """
    )
    @APIResponse(
        responseCode = "200",
        description = "Lista de queries disponíveis.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                name = "Exemplo de catálogo",
                value = """
                        [
                          {
                            "key": "sales-summary",
                            "version": "1.0.0",
                            "description": "Resumo de vendas por período (total faturado e total de pedidos)",
                            "endpoint": "/queries/sales-summary/execute",
                            "tags": ["vendas", "resumo"],
                            "params": [
                              { "name": "cnpj",      "type": "STRING", "required": true,  "description": "CNPJ da loja" },
                              { "name": "startDate", "type": "DATE",   "required": true,  "description": "Data de início (yyyy-MM-dd)" },
                              { "name": "endDate",   "type": "DATE",   "required": true,  "description": "Data de fim (yyyy-MM-dd)" }
                            ],
                            "pagination": {
                              "defaultPageSize": 1,
                              "maxPageSize": 1,
                              "allowUnpaged": true,
                              "maxUnpagedRows": 1
                            }
                          },
                          {
                            "key": "top-sellers",
                            "version": "1.0.0",
                            "description": "Top vendedores por faturamento no período",
                            "endpoint": "/queries/top-sellers/execute",
                            "tags": ["vendas", "vendedores", "ranking"],
                            "params": [
                              { "name": "cnpj",      "type": "STRING",  "required": true,  "description": "CNPJ da loja" },
                              { "name": "startDate", "type": "DATE",    "required": true,  "description": "Data de início (yyyy-MM-dd)" },
                              { "name": "endDate",   "type": "DATE",    "required": true,  "description": "Data de fim (yyyy-MM-dd)" },
                              { "name": "limit",     "type": "INTEGER", "required": false, "description": "Máximo de vendedores a retornar", "defaultValue": "5" }
                            ],
                            "pagination": {
                              "defaultPageSize": 10,
                              "maxPageSize": 50,
                              "allowUnpaged": true,
                              "maxUnpagedRows": 500
                            }
                          }
                        ]
                        """
            )
        )
    )
    @APIResponse(responseCode = "401", description = "X-API-Key ausente.")
    @APIResponse(responseCode = "403", description = "X-API-Key inválida.")
    public List<QueryInfoResponse> listQueries() {
        return listQueriesUseCase.execute().stream()
                .map(QueryInfoResponse::from)
                .toList();
    }

    @GET
    @Path("/{key}")
    @Operation(
        summary = "Detalhes de uma query específica",
        description = """
                Retorna os metadados completos de uma query pelo seu identificador (`key`).
                Útil para inspecionar os parâmetros exigidos antes de executar via `POST /queries/{key}/execute`.
                """
    )
    @Parameter(
        name = "key",
        description = "Identificador único da query (ex: `sales-summary`, `top-sellers`, `stock-search`).",
        required = true,
        example = "top-sellers"
    )
    @APIResponse(
        responseCode = "200",
        description = "Metadados da query encontrada.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                name = "top-sellers",
                value = """
                        {
                          "key": "top-sellers",
                          "version": "1.0.0",
                          "description": "Top vendedores por faturamento no período",
                          "endpoint": "/queries/top-sellers/execute",
                          "tags": ["vendas", "vendedores", "ranking"],
                          "params": [
                            { "name": "cnpj",      "type": "STRING",  "required": true,  "description": "CNPJ da loja" },
                            { "name": "startDate", "type": "DATE",    "required": true,  "description": "Data de início (yyyy-MM-dd)" },
                            { "name": "endDate",   "type": "DATE",    "required": true,  "description": "Data de fim (yyyy-MM-dd)" },
                            { "name": "limit",     "type": "INTEGER", "required": false, "description": "Máximo de vendedores", "defaultValue": "5" }
                          ],
                          "pagination": {
                            "defaultPageSize": 10,
                            "maxPageSize": 50,
                            "allowUnpaged": true,
                            "maxUnpagedRows": 500
                          }
                        }
                        """
            )
        )
    )
    @APIResponse(responseCode = "401", description = "X-API-Key ausente.")
    @APIResponse(responseCode = "403", description = "X-API-Key inválida.")
    @APIResponse(
        responseCode = "404",
        description = "Query não encontrada.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                value = """
                        {
                          "error": "QueryNotFoundException",
                          "message": "Query não encontrada: top-sellers-inexistente",
                          "details": null,
                          "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                        }
                        """
            )
        )
    )
    public QueryInfoResponse getQueryDetails(@PathParam("key") String key) {
        QueryDefinition definition = getQueryDetailsUseCase.execute(key);
        return QueryInfoResponse.from(definition);
    }
}

