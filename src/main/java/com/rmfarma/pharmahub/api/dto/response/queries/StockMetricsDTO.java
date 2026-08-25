package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record StockMetricsDTO(
        String cnpj,
        String grupoEconomico,
        BigDecimal totalCustoEstoque,
        BigDecimal totalItensAltaRotatividade
) {
}

