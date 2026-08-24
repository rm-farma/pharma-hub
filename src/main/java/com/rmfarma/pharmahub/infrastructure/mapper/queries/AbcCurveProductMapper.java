package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.AbcCurveProductDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("abc-curve-products")
public class AbcCurveProductMapper implements RowMapper<AbcCurveProductDTO> {

    @Override
    public AbcCurveProductDTO map(FieldValueList row) {
        return new AbcCurveProductDTO(
                BigQueryValues.string(row, "ean"),
                BigQueryValues.string(row, "apresentacao"),
                BigQueryValues.numeric(row, "faturamento_total"),
                BigQueryValues.numeric(row, "quantidade_vendida"),
                BigQueryValues.longValue(row, "num_transacoes"),
                BigQueryValues.numeric(row, "percentual_individual"),
                BigQueryValues.numeric(row, "percentual_acumulado"),
                BigQueryValues.string(row, "classe_abc"),
                BigQueryValues.numeric(row, "saldo_estoque"),
                BigQueryValues.numeric(row, "preco_venda"),
                BigQueryValues.numeric(row, "custo_medio")
        );
    }
}
