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
        String totalSkus,
        String totalUnidades,
        String valorTotalCusto,
        String valorTotalVenda
) {
}

