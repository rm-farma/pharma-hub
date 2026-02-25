package com.rmfarma.pharmahub.api.dto.response;

public record ErrorResponse(
        String error,
        String message,
        String details,
        String requestId
) {
}

