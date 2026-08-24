package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record IdleStockDTO(
        String ean,
        String apresentacao,
        String fabricante,
        String grupoMacro,
        BigDecimal saldoEstoque,
        BigDecimal custoMedioTotal,
        BigDecimal precoVenda,
        Long totalSkus,
        BigDecimal totalUnidades,
        BigDecimal valorTotalCusto,
        BigDecimal valorTotalVenda
) {
}

