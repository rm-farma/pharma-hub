package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.StockSearchDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("stock-search")
public class StockSearchMapper implements RowMapper<StockSearchDTO> {

    @Override
    public StockSearchDTO map(FieldValueList row) {
        return new StockSearchDTO(
                BigQueryValues.string(row, "ean"),
                BigQueryValues.string(row, "apresentacao"),
                BigQueryValues.string(row, "fabricante"),
                BigQueryValues.string(row, "grupo_macro"),
                BigQueryValues.numeric(row, "saldo_estoque"),
                BigQueryValues.numeric(row, "custo_medio"),
                BigQueryValues.numeric(row, "custo_medio_total"),
                BigQueryValues.numeric(row, "preco_venda")
        );
    }
}
