package com.rmfarma.pharmahub.core.model;

import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.StandardSQLTypeName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum ParamType {

    STRING(StandardSQLTypeName.STRING) {
        @Override
        public Object convert(String value) {
            return value;
        }
    },
    INTEGER(StandardSQLTypeName.INT64) {
        @Override
        public Object convert(String value) {
            return Integer.parseInt(value);
        }
    },
    LONG(StandardSQLTypeName.INT64) {
        @Override
        public Object convert(String value) {
            return Long.parseLong(value);
        }
    },
    DECIMAL(StandardSQLTypeName.NUMERIC) {
        @Override
        public Object convert(String value) {
            return new BigDecimal(value);
        }
    },
    BOOLEAN(StandardSQLTypeName.BOOL) {
        @Override
        public Object convert(String value) {
            return Boolean.parseBoolean(value);
        }
    },
    DATE(StandardSQLTypeName.DATE) {
        @Override
        public Object convert(String value) {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        }
    },
    TIMESTAMP(StandardSQLTypeName.TIMESTAMP) {
        @Override
        public Object convert(String value) {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    };

    private final StandardSQLTypeName sqlType;

    ParamType(StandardSQLTypeName sqlType) {
        this.sqlType = sqlType;
    }

    public abstract Object convert(String value);

    public QueryParameterValue toQueryParameterValue(Object value) {
        return QueryParameterValue.of(value, sqlType);
    }

    public static ParamType fromString(String type) {
        return ParamType.valueOf(type.toUpperCase());
    }
}
