package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.TopSellerDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("top-sellers")
public class TopSellerMapper implements RowMapper<TopSellerDTO> {

    @Override
    public TopSellerDTO map(FieldValueList row) {
        return new TopSellerDTO(
                BigQueryValues.string(row, "seller"),
                BigQueryValues.numeric(row, "totalAmount"),
                BigQueryValues.longValue(row, "totalOrders")
        );
    }
}
