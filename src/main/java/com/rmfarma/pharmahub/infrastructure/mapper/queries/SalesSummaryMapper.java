package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.SalesSummaryDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("sales-summary")
public class SalesSummaryMapper implements RowMapper<SalesSummaryDTO> {

    @Override
    public SalesSummaryDTO map(FieldValueList row) {
        return new SalesSummaryDTO(
                BigQueryValues.numeric(row, "totalAmount"),
                BigQueryValues.longValue(row, "totalOrders")
        );
    }
}
