package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record TopSellerDTO(String seller, BigDecimal totalAmount, Long totalOrders) {
}

