package com.rmfarma.pharmahub.infrastructure.mapper;

import com.google.cloud.bigquery.FieldValueList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
@Named("generic")
public class GenericMapMapper implements RowMapper<Map<String, Object>> {

    // FieldValueList não expõe os nomes de coluna sem o Schema da query (não disponível aqui),
    // por isso o fallback genérico usa chaves posicionais.
    @Override
    public Map<String, Object> map(FieldValueList row) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < row.size(); i++) {
            result.put("field_" + i, row.get(i).isNull() ? null : row.get(i).getValue());
        }
        return result;
    }
}
