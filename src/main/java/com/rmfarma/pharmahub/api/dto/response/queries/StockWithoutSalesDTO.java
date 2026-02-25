package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record StockWithoutSalesDTO(
        String ean,
        String apresentacao,
        String fabricante,
        String grupoMacro,
        BigDecimal saldoEstoque,
        BigDecimal custoMedioTotal,
        BigDecimal precoVenda
) {
}

