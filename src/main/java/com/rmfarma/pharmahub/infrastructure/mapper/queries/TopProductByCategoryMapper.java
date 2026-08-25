package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.TopProductByCategoryDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("top-products-by-category")
public class TopProductByCategoryMapper implements RowMapper<TopProductByCategoryDTO> {

    @Override
    public TopProductByCategoryDTO map(FieldValueList row) {
        return new TopProductByCategoryDTO(
                BigQueryValues.string(row, "ean"),
                BigQueryValues.string(row, "productName"),
                BigQueryValues.numeric(row, "totalQuantity"),
                BigQueryValues.numeric(row, "totalAmount"),
                BigQueryValues.longValue(row, "totalOrders")
        );
    }
}
