package com.rmfarma.pharmahub.api.dto.request;

import java.util.Map;

public record ExecuteRequest(
        Map<String, Object> params,
        Integer page,
        Integer pageSize,
        Boolean unpaged
) {
}

