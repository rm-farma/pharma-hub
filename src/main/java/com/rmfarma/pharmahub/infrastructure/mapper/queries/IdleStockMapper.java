package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.IdleStockDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("idle-stock")
public class IdleStockMapper implements RowMapper<IdleStockDTO> {

    @Override
    public IdleStockDTO map(FieldValueList row) {
        return new IdleStockDTO(
                BigQueryValues.string(row, "ean"),
                BigQueryValues.string(row, "apresentacao"),
                BigQueryValues.string(row, "fabricante"),
                BigQueryValues.string(row, "grupo_macro"),
                BigQueryValues.numeric(row, "saldo_estoque"),
                BigQueryValues.numeric(row, "custo_medio_total"),
                BigQueryValues.numeric(row, "preco_venda"),
                BigQueryValues.longValue(row, "total_skus"),
                BigQueryValues.numeric(row, "total_unidades"),
                BigQueryValues.numeric(row, "valor_total_custo"),
                BigQueryValues.numeric(row, "valor_total_venda")
        );
    }
}
