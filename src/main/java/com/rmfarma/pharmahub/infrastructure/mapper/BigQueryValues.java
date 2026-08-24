package com.rmfarma.pharmahub.infrastructure.mapper;

import com.google.cloud.bigquery.FieldValueList;

import java.math.BigDecimal;

/**
 * Extração null-safe de colunas de um FieldValueList — FieldValue.getXxxValue() lança
 * exceção quando a coluna é NULL, diferente do java.sql.ResultSet.getXxx() que os
 * mappers usavam antes.
 */
public final class BigQueryValues {

    private BigQueryValues() {
    }

    public static String string(FieldValueList row, String column) {
        var value = row.get(column);
        return value.isNull() ? null : value.getStringValue();
    }

    public static BigDecimal numeric(FieldValueList row, String column) {
        var value = row.get(column);
        return value.isNull() ? null : value.getNumericValue();
    }

    public static Long longValue(FieldValueList row, String column) {
        var value = row.get(column);
        return value.isNull() ? null : value.getLongValue();
    }

    public static Boolean booleanValue(FieldValueList row, String column) {
        var value = row.get(column);
        return value.isNull() ? null : value.getBooleanValue();
    }
}
