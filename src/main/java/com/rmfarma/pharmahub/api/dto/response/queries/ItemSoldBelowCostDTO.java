package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record ItemSoldBelowCostDTO(
        String ean,
        String productName,
        String notaFiscal,
        String dataVenda,
        BigDecimal totalQuantity,
        BigDecimal faturamento,
        BigDecimal custo
) {
}
