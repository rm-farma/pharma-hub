package com.rmfarma.pharmahub.application;

import com.rmfarma.pharmahub.core.model.QueryDefinition;
import com.rmfarma.pharmahub.core.port.QueryRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ListQueriesUseCase {

    private final QueryRepository queryRepository;

    public ListQueriesUseCase(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public List<QueryDefinition> execute() {
        return queryRepository.findAll();
    }
}

