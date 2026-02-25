package com.rmfarma.pharmahub.api.dto.response;

import java.util.List;

public record UnpagedResponse<T>(
        String queryKey,
        String mode,
        int returnedItems,
        boolean truncated,
        String truncatedMessage,
        List<T> items,
        long durationMs,
        String requestId
) {
}

