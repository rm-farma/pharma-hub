package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record ProductsLossDTO(
        String ean,
        String productName,
        BigDecimal totalQuantity,
        BigDecimal faturamento,
        BigDecimal custo
) {
}
