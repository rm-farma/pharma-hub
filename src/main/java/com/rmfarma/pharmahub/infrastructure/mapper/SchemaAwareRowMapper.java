package com.rmfarma.pharmahub.infrastructure.mapper;

import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Schema;

/**
 * Mapper que precisa do Schema da query para montar o resultado — o FieldValueList
 * sozinho não expõe os nomes das colunas.
 */
public interface SchemaAwareRowMapper<T> extends RowMapper<T> {

    T map(FieldValueList row, Schema schema);
}
