package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.google.cloud.bigquery.FieldValueList;
import com.rmfarma.pharmahub.api.dto.response.queries.ItemSoldBelowCostDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.BigQueryValues;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("items-sold-below-cost")
public class ItemSoldBelowCostMapper implements RowMapper<ItemSoldBelowCostDTO> {

    @Override
    public ItemSoldBelowCostDTO map(FieldValueList row) {
        return new ItemSoldBelowCostDTO(
                BigQueryValues.string(row, "ean"),
                BigQueryValues.string(row, "productName"),
                BigQueryValues.string(row, "notaFiscal"),
                BigQueryValues.string(row, "dataVenda"),
                BigQueryValues.numeric(row, "totalQuantity"),
                BigQueryValues.numeric(row, "faturamento"),
                BigQueryValues.numeric(row, "custo")
        );
    }
}
