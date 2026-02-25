package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record SalesOverviewDTO(BigDecimal totalAmount, BigDecimal cmv, Long totalOrders) {
}

