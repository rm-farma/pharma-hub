package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.TopProductDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("top-products")
public class TopProductMapper implements RowMapper<TopProductDTO> {

    @Override
    public TopProductDTO map(FieldValueList row) {
        return new TopProductDTO(
                BigQueryValues.string(row, "productName"),
                BigQueryValues.numeric(row, "totalQuantity"),
                BigQueryValues.numeric(row, "totalAmount")
        );
    }
}
