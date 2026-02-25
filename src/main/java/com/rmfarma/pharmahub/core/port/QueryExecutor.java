package com.rmfarma.pharmahub.core.port;

import com.rmfarma.pharmahub.core.model.PagedResult;
import com.rmfarma.pharmahub.core.model.QueryDefinition;
import com.rmfarma.pharmahub.core.model.UnpagedResult;

import java.util.Map;

public interface QueryExecutor {

    <T> PagedResult<T> executePaged(QueryDefinition definition, Map<String, Object> params, int page, int pageSize);

    <T> UnpagedResult<T> executeUnpaged(QueryDefinition definition, Map<String, Object> params, int maxRows);
}

