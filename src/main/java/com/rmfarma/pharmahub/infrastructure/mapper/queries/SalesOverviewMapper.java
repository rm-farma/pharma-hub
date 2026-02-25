package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.SalesOverviewDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("sales-overview")
public class SalesOverviewMapper implements ResultSetMapper<SalesOverviewDTO> {

    @Override
    public SalesOverviewDTO map(ResultSet rs) throws SQLException {
        return new SalesOverviewDTO(
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("cmv"),
                rs.getLong("total_orders")
        );
    }
}

