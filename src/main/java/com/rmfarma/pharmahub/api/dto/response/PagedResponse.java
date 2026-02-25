package com.rmfarma.pharmahub.api.dto.response;

import java.util.List;

public record PagedResponse<T>(
        String queryKey,
        String mode,
        int page,
        int pageSize,
        long totalItems,
        int totalPages,
        List<T> items,
        long durationMs,
        String requestId
) {
}

