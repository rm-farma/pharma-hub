package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.ProductsLossDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("products-loss")
public class ProductsLossMapper implements RowMapper<ProductsLossDTO> {

    @Override
    public ProductsLossDTO map(FieldValueList row) {
        return new ProductsLossDTO(
                BigQueryValues.string(row, "ean"),
                BigQueryValues.string(row, "productName"),
                BigQueryValues.numeric(row, "totalQuantity"),
                BigQueryValues.numeric(row, "faturamento"),
                BigQueryValues.numeric(row, "custo")
        );
    }
}
