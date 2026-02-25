package com.rmfarma.pharmahub.core.port;

import com.rmfarma.pharmahub.core.model.QueryDefinition;

import java.util.List;
import java.util.Optional;

public interface QueryRepository {

    Optional<QueryDefinition> findByKey(String key);

    List<QueryDefinition> findAll();
}

