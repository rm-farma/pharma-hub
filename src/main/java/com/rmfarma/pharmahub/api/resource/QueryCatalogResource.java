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

import java.util.List;

@Path("/queries")
@Produces(MediaType.APPLICATION_JSON)
public class QueryCatalogResource {

    @Inject
    ListQueriesUseCase listQueriesUseCase;

    @Inject
    GetQueryDetailsUseCase getQueryDetailsUseCase;

    @GET
    public List<QueryInfoResponse> listQueries() {
        return listQueriesUseCase.execute().stream()
                .map(QueryInfoResponse::from)
                .toList();
    }

    @GET
    @Path("/{key}")
    public QueryInfoResponse getQueryDetails(@PathParam("key") String key) {
        QueryDefinition definition = getQueryDetailsUseCase.execute(key);
        return QueryInfoResponse.from(definition);
    }
}

