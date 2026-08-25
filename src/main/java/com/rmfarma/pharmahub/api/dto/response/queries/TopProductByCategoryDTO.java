package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record TopProductByCategoryDTO(
        String ean,
        String productName,
        BigDecimal totalQuantity,
        BigDecimal totalAmount,
        Long totalOrders
) {
}
