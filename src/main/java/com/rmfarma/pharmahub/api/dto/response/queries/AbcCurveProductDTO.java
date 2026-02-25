package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record AbcCurveProductDTO(
        String ean,
        String apresentacao,
        BigDecimal faturamentoTotal,
        BigDecimal quantidadeVendida,
        Long numTransacoes,
        BigDecimal percentualIndividual,
        BigDecimal percentualAcumulado,
        String classeAbc,
        BigDecimal saldoEstoque,
        BigDecimal precoVenda,
        BigDecimal custoMedio
) {
}

