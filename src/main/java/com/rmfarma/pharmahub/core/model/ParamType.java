package com.rmfarma.pharmahub.core.model;

import com.google.cloud.bigquery.QueryParameterValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum ParamType {

    STRING {
        @Override
        public Object convert(String value) {
            return value;
        }

        @Override
        public QueryParameterValue toQueryParameterValue(Object value) {
            return QueryParameterValue.string((String) value);
        }
    },
    INTEGER {
        @Override
        public Object convert(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public QueryParameterValue toQueryParameterValue(Object value) {
            return QueryParameterValue.int64(value == null ? null : ((Number) value).longValue());
        }
    },
    LONG {
        @Override
        public Object convert(String value) {
            return Long.parseLong(value);
        }

        @Override
        public QueryParameterValue toQueryParameterValue(Object value) {
            return QueryParameterValue.int64(value == null ? null : ((Number) value).longValue());
        }
    },
    DECIMAL {
        @Override
        public Object convert(String value) {
            return new BigDecimal(value);
        }

        @Override
        public QueryParameterValue toQueryParameterValue(Object value) {
            return QueryParameterValue.numeric(value == null ? null : new BigDecimal(value.toString()));
        }
    },
    BOOLEAN {
        @Override
        public Object convert(String value) {
            return Boolean.parseBoolean(value);
        }

        @Override
        public QueryParameterValue toQueryParameterValue(Object value) {
            return QueryParameterValue.bool((Boolean) value);
        }
    },
    // A API do BigQuery espera DATE/TIMESTAMP como String formatada, não como
    // java.time.* — QueryParameterValue.of(Object, StandardSQLTypeName) rejeita
    // LocalDate/LocalDateTime com "Type DATE incompatible with java.time.LocalDate".
    DATE {
        @Override
        public Object convert(String value) {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        }

        @Override
        public QueryParameterValue toQueryParameterValue(Object value) {
            if (value == null) {
                return QueryParameterValue.date((String) null);
            }
            LocalDate date = value instanceof LocalDate d ? d : LocalDate.parse(value.toString());
            return QueryParameterValue.date(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    },
    TIMESTAMP {
        @Override
        public Object convert(String value) {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        @Override
        public QueryParameterValue toQueryParameterValue(Object value) {
            if (value == null) {
                return QueryParameterValue.timestamp((String) null);
            }
            LocalDateTime dateTime = value instanceof LocalDateTime dt ? dt : LocalDateTime.parse(value.toString());
            return QueryParameterValue.timestamp(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    };

    public abstract Object convert(String value);

    public abstract QueryParameterValue toQueryParameterValue(Object value);

    public static ParamType fromString(String type) {
        return ParamType.valueOf(type.toUpperCase());
    }
}
