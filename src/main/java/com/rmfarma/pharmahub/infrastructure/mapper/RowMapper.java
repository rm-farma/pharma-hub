package com.rmfarma.pharmahub.infrastructure.mapper;

import com.google.cloud.bigquery.FieldValueList;

@FunctionalInterface
public interface RowMapper<T> {

    T map(FieldValueList row);
}
