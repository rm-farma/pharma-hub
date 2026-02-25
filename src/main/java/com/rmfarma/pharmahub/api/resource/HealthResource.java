package com.rmfarma.pharmahub.api.resource;

import io.agroal.api.AgroalDataSource;
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

@Path("/health")
@Tag(name = "Health", description = "Verificação de saúde da aplicação e conectividade com o banco de dados.")
public class HealthResource {

    @Inject
    AgroalDataSource dataSource;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Health check",
        description = "Verifica se a aplicação está no ar e se a conexão com o PostgreSQL está ativa. " +
                      "Este endpoint **não requer autenticação** (sem X-API-Key)."
    )
    @APIResponse(
        responseCode = "200",
        description = "Aplicação e banco de dados operacionais.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                name = "UP",
                value = """
                        {
                          "status": "UP",
                          "database": "connected"
                        }
                        """
            )
        )
    )
    @APIResponse(
        responseCode = "500",
        description = "Banco de dados inacessível.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            examples = @ExampleObject(
                name = "DOWN",
                value = """
                        {
                          "status": "DOWN",
                          "database": "disconnected",
                          "error": "Connection refused"
                        }
                        """
            )
        )
    )
    public Response health() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            if (rs.next()) {
                return Response.ok(Map.of(
                        "status", "UP",
                        "database", "connected"
                )).build();
            }
        } catch (Exception e) {
            return Response.serverError().entity(Map.of(
                    "status", "DOWN",
                    "database", "disconnected",
                    "error", e.getMessage()
            )).build();
        }

        return Response.serverError().entity(Map.of(
                "status", "DOWN",
                "database", "unknown"
        )).build();
    }
}

