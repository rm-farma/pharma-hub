package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.StockMetricsDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("stock-metrics")
public class StockMetricsMapper implements RowMapper<StockMetricsDTO> {

    @Override
    public StockMetricsDTO map(FieldValueList row) {
        return new StockMetricsDTO(
                BigQueryValues.string(row, "cnpj"),
                BigQueryValues.string(row, "grupo_economico"),
                BigQueryValues.numeric(row, "total_custo_estoque"),
                BigQueryValues.numeric(row, "total_itens_alta_rotatividade")
        );
    }
}
