package com.rmfarma.pharmahub.api.filter;

import com.rmfarma.pharmahub.infrastructure.config.ApiKeyConfig;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Map;

@Provider
@PreMatching
public class ApiKeyFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(ApiKeyFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String CLIENT_ID_HEADER = "X-Client-Id";

    @Inject
    ApiKeyConfig apiKeyConfig;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        // Libera /health e /q/* (métricas, health do SmallRye, dev-ui)
        if (path.equals("/health") || path.startsWith("/q/") || path.startsWith("q/")) {
            return;
        }

        String apiKey = requestContext.getHeaderString(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            LOG.warn("Requisição sem API Key");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"UNAUTHORIZED\",\"message\":\"Header X-API-Key é obrigatório\"}")
                    .build());
            return;
        }

        String clientId = resolveClientId(apiKey);
        if (clientId == null) {
            LOG.warnv("API Key inválida: {0}", apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"FORBIDDEN\",\"message\":\"API Key inválida\"}")
                    .build());
            return;
        }

        requestContext.getHeaders().putSingle(CLIENT_ID_HEADER, clientId);
    }

    private String resolveClientId(String apiKey) {
        for (Map.Entry<String, String> entry : apiKeyConfig.apiKeys().entrySet()) {
            if (entry.getValue().equals(apiKey)) {
                return entry.getKey();
            }
        }
        return null;
    }
}

