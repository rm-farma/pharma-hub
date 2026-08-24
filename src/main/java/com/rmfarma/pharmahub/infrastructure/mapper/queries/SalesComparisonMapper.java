package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.SalesComparisonDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("sales-comparison")
public class SalesComparisonMapper implements RowMapper<SalesComparisonDTO> {

    @Override
    public SalesComparisonDTO map(FieldValueList row) {
        return new SalesComparisonDTO(
                BigQueryValues.string(row, "periodoBase"),
                BigQueryValues.string(row, "periodoComparado"),
                BigQueryValues.numeric(row, "faturamentoBase"),
                BigQueryValues.numeric(row, "faturamentoComparado"),
                BigQueryValues.numeric(row, "variacaoFaturamento"),
                BigQueryValues.numeric(row, "itensVendidosBase"),
                BigQueryValues.numeric(row, "itensVendidosComparado"),
                BigQueryValues.numeric(row, "variacaoItensVendidos"),
                BigQueryValues.numeric(row, "ticketMedioBase"),
                BigQueryValues.numeric(row, "ticketMedioComparado"),
                BigQueryValues.numeric(row, "variacaoTicketMedio")
        );
    }
}
