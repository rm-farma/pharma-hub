package com.rmfarma.pharmahub.infrastructure.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
@Named("generic")
public class GenericMapMapper implements ResultSetMapper<Map<String, Object>> {

    @Override
    public Map<String, Object> map(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            String label = meta.getColumnLabel(i);
            row.put(label, rs.getObject(i));
        }
        return row;
    }
}

