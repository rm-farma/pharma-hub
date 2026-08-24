package com.rmfarma.pharmahub.api.resource;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/health")
@Tag(name = "Health", description = "Verificação de saúde da aplicação e conectividade com o BigQuery.")
public class HealthResource {

    private static final String BIGQUERY_PROJECT = "rm-farma-dw-prod";
    private static final String BIGQUERY_DATASET = "licenciado";

    @Inject
    BigQuery bigquery;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Health check",
        description = "Verifica se a aplicação está no ar e se a conexão com o BigQuery está ativa. " +
                      "Este endpoint **não requer autenticação** (sem X-API-Key)."
    )
    @APIResponse(
        responseCode = "200",
        description = "Aplicação e BigQuery operacionais.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                name = "UP",
                value = """
                        {
                          "status": "UP",
                          "bigquery": "connected"
                        }
                        """
            )
        )
    )
    @APIResponse(
        responseCode = "500",
        description = "BigQuery inacessível.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                name = "DOWN",
                value = """
                        {
                          "status": "DOWN",
                          "bigquery": "disconnected",
                          "error": "Permission denied"
                        }
                        """
            )
        )
    )
    public Response health() {
        try {
            Dataset dataset = bigquery.getDataset(DatasetId.of(BIGQUERY_PROJECT, BIGQUERY_DATASET));
            if (dataset != null) {
                return Response.ok(Map.of(
                        "status", "UP",
                        "bigquery", "connected"
                )).build();
            }
            return Response.serverError().entity(Map.of(
                    "status", "DOWN",
                    "bigquery", "dataset " + BIGQUERY_PROJECT + "." + BIGQUERY_DATASET + " não encontrado"
            )).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of(
                    "status", "DOWN",
                    "bigquery", "disconnected",
                    "error", e.getMessage()
            )).build();
        }
    }
}
