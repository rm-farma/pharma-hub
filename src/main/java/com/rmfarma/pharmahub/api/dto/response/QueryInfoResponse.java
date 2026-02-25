package com.rmfarma.pharmahub.api.dto.response;

import com.rmfarma.pharmahub.core.model.ParamDefinition;

import java.util.List;

public record QueryInfoResponse(
        String key,
        String version,
        String description,
        String endpoint,
        List<String> tags,
        List<ParamInfo> params,
        PaginationInfo pagination
) {
    public record ParamInfo(
            String name,
            String type,
            boolean required,
            String description,
            String defaultValue
    ) {
    }

    public record PaginationInfo(
            int defaultPageSize,
            int maxPageSize,
            boolean allowUnpaged,
            int maxUnpagedRows
    ) {
    }

    public static QueryInfoResponse from(com.rmfarma.pharmahub.core.model.QueryDefinition def) {
        List<ParamInfo> paramInfos = def.params().stream()
                .map(p -> new ParamInfo(p.name(), p.type().name(), p.required(), p.description(), p.defaultValue()))
                .toList();

        PaginationInfo paginationInfo = new PaginationInfo(
                def.defaultPageSize(), def.maxPageSize(), def.allowUnpaged(), def.maxUnpagedRows()
        );

        return new QueryInfoResponse(
                def.key(), def.version(), def.description(),
                def.endpoint(), def.tags(), paramInfos, paginationInfo
        );
    }
}

