package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record SalesComparisonDTO(
        String periodoBase,
        String periodoComparado,
        BigDecimal faturamentoBase,
        BigDecimal faturamentoComparado,
        BigDecimal variacaoFaturamento,
        BigDecimal itensVendidosBase,
        BigDecimal itensVendidosComparado,
        BigDecimal variacaoItensVendidos,
        BigDecimal ticketMedioBase,
        BigDecimal ticketMedioComparado,
        BigDecimal variacaoTicketMedio
) {
}

