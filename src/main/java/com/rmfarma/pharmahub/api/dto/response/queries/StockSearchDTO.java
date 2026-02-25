package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record StockSearchDTO(
        String ean,
        String apresentacao,
        String fabricante,
        String grupoMacro,
        BigDecimal saldoEstoque,
        BigDecimal custoMedio,
        BigDecimal custoMedioTotal,
        BigDecimal precoVenda
) {
}

