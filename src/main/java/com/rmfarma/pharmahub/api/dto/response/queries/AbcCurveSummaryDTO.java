package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record AbcCurveSummaryDTO(
        Long totalProdutos,
        Long totalProdutosA,
        Long totalProdutosB,
        Long totalProdutosC,
        BigDecimal faturamentoTotal,
        BigDecimal faturamentoA,
        BigDecimal faturamentoB,
        BigDecimal faturamentoC
) {
}

