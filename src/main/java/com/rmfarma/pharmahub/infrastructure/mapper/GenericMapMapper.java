package com.rmfarma.pharmahub.infrastructure.mapper;

import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Schema;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fallback usado quando a query não tem mapper específico. Usa o Schema da query para
 * nomear as colunas com o nome original vindo do BigQuery; só cai em chaves posicionais
 * ({@code field_N}) quando o schema não está disponível.
 */
@ApplicationScoped
@Named("generic")
public class GenericMapMapper implements SchemaAwareRowMapper<Map<String, Object>> {

    @Override
    public Map<String, Object> map(FieldValueList row) {
        return map(row, null);
    }

    @Override
    public Map<String, Object> map(FieldValueList row, Schema schema) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < row.size(); i++) {
            var value = row.get(i);
            result.put(columnName(schema, i), value.isNull() ? null : value.getValue());
        }
        return result;
    }

    private String columnName(Schema schema, int index) {
        if (schema == null || schema.getFields() == null || index >= schema.getFields().size()) {
            return "field_" + index;
        }
        String name = schema.getFields().get(index).getName();
        return (name == null || name.isBlank()) ? "field_" + index : name;
    }
}
