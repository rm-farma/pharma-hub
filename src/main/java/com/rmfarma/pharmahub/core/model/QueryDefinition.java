package com.rmfarma.pharmahub.core.model;

import java.util.List;

public record QueryDefinition(
        String key,
        String version,
        String description,
        String endpoint,
        List<String> tags,
        String sqlTemplate,
        List<ParamDefinition> params,
        int defaultPageSize,
        int maxPageSize,
        boolean allowUnpaged,
        int maxUnpagedRows,
        long timeoutMs,
        String dtoClassName,
        String mapperClassName
) {
}

