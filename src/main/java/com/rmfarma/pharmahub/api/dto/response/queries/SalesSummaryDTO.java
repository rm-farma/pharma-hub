package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record SalesSummaryDTO(BigDecimal totalAmount, Long totalOrders) {
}

