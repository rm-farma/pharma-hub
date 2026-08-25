package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record ManufacturerSalesDTO(
        String ean,
        String productName,
        BigDecimal totalQuantity,
        BigDecimal totalAmount,
        Long totalOrders
) {
}
