package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.SalesOverviewDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("sales-overview")
public class SalesOverviewMapper implements RowMapper<SalesOverviewDTO> {

    @Override
    public SalesOverviewDTO map(FieldValueList row) {
        return new SalesOverviewDTO(
                BigQueryValues.numeric(row, "totalAmount"),
                BigQueryValues.numeric(row, "cmv"),
                BigQueryValues.longValue(row, "totalOrders")
        );
    }
}
