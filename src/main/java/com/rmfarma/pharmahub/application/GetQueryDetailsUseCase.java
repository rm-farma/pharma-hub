package com.rmfarma.pharmahub.application;

import com.rmfarma.pharmahub.core.exception.QueryNotFoundException;
import com.rmfarma.pharmahub.core.model.QueryDefinition;
import com.rmfarma.pharmahub.core.port.QueryRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GetQueryDetailsUseCase {

    private final QueryRepository queryRepository;

    public GetQueryDetailsUseCase(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public QueryDefinition execute(String key) {
        return queryRepository.findByKey(key)
                .orElseThrow(() -> new QueryNotFoundException(key));
    }
}

