package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.ManufacturerSalesDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("manufacturer-sales")
public class ManufacturerSalesMapper implements RowMapper<ManufacturerSalesDTO> {

    @Override
    public ManufacturerSalesDTO map(FieldValueList row) {
        return new ManufacturerSalesDTO(
                BigQueryValues.string(row, "ean"),
                BigQueryValues.string(row, "productName"),
                BigQueryValues.numeric(row, "totalQuantity"),
                BigQueryValues.numeric(row, "totalAmount"),
                BigQueryValues.longValue(row, "totalOrders")
        );
    }
}
