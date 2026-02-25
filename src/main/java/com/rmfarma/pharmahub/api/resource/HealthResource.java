package com.rmfarma.pharmahub.api.resource;

import io.agroal.api.AgroalDataSource;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

@Path("/health")
public class HealthResource {

    @Inject
    AgroalDataSource dataSource;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
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

