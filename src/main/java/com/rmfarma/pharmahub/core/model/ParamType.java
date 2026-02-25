package com.rmfarma.pharmahub.core.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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
        public void bind(PreparedStatement ps, int index, Object value) throws SQLException {
            if (value == null) {
                ps.setNull(index, Types.VARCHAR);
            } else {
                ps.setString(index, value.toString());
            }
        }
    },
    INTEGER {
        @Override
        public Object convert(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public void bind(PreparedStatement ps, int index, Object value) throws SQLException {
            if (value == null) {
                ps.setNull(index, Types.INTEGER);
            } else {
                ps.setInt(index, ((Number) value).intValue());
            }
        }
    },
    LONG {
        @Override
        public Object convert(String value) {
            return Long.parseLong(value);
        }

        @Override
        public void bind(PreparedStatement ps, int index, Object value) throws SQLException {
            if (value == null) {
                ps.setNull(index, Types.BIGINT);
            } else {
                ps.setLong(index, ((Number) value).longValue());
            }
        }
    },
    DECIMAL {
        @Override
        public Object convert(String value) {
            return new BigDecimal(value);
        }

        @Override
        public void bind(PreparedStatement ps, int index, Object value) throws SQLException {
            if (value == null) {
                ps.setNull(index, Types.DECIMAL);
            } else {
                ps.setBigDecimal(index, new BigDecimal(value.toString()));
            }
        }
    },
    BOOLEAN {
        @Override
        public Object convert(String value) {
            return java.lang.Boolean.parseBoolean(value);
        }

        @Override
        public void bind(PreparedStatement ps, int index, Object value) throws SQLException {
            if (value == null) {
                ps.setNull(index, Types.BOOLEAN);
            } else {
                ps.setBoolean(index, (Boolean) value);
            }
        }
    },
    DATE {
        @Override
        public Object convert(String value) {
            return Date.valueOf(LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE));
        }

        @Override
        public void bind(PreparedStatement ps, int index, Object value) throws SQLException {
            if (value == null) {
                ps.setNull(index, Types.DATE);
            } else if (value instanceof Date d) {
                ps.setDate(index, d);
            } else {
                ps.setDate(index, Date.valueOf(value.toString()));
            }
        }
    },
    TIMESTAMP {
        @Override
        public Object convert(String value) {
            return Timestamp.valueOf(LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        @Override
        public void bind(PreparedStatement ps, int index, Object value) throws SQLException {
            if (value == null) {
                ps.setNull(index, Types.TIMESTAMP);
            } else if (value instanceof Timestamp t) {
                ps.setTimestamp(index, t);
            } else {
                ps.setTimestamp(index, Timestamp.valueOf(value.toString()));
            }
        }
    };

    public abstract Object convert(String value);

    public abstract void bind(PreparedStatement ps, int index, Object value) throws SQLException;

    public static ParamType fromString(String type) {
        return ParamType.valueOf(type.toUpperCase());
    }
}

