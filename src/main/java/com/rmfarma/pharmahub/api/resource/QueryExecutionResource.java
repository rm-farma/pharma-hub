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
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;

@Path("/queries/{key}/execute")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QueryExecutionResource {

    private static final Logger LOG = Logger.getLogger(QueryExecutionResource.class);

    @Inject
    ExecuteQueryUseCase executeQueryUseCase;

    @POST
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
                    paged.hasNext(), paged.items(), durationMs, requestId
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

