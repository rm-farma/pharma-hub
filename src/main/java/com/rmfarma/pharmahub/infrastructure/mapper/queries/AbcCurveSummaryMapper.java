package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.AbcCurveSummaryDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("abc-curve-summary")
public class AbcCurveSummaryMapper implements RowMapper<AbcCurveSummaryDTO> {

    @Override
    public AbcCurveSummaryDTO map(FieldValueList row) {
        return new AbcCurveSummaryDTO(
                BigQueryValues.longValue(row, "total_produtos"),
                BigQueryValues.longValue(row, "total_produtos_a"),
                BigQueryValues.longValue(row, "total_produtos_b"),
                BigQueryValues.longValue(row, "total_produtos_c"),
                BigQueryValues.numeric(row, "faturamento_total"),
                BigQueryValues.numeric(row, "faturamento_a"),
                BigQueryValues.numeric(row, "faturamento_b"),
                BigQueryValues.numeric(row, "faturamento_c")
        );
    }
}
