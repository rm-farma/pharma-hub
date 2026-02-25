package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.SalesSummaryDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("sales-summary")
public class SalesSummaryMapper implements ResultSetMapper<SalesSummaryDTO> {

    @Override
    public SalesSummaryDTO map(ResultSet rs) throws SQLException {
        return new SalesSummaryDTO(
                rs.getBigDecimal("total_amount"),
                rs.getLong("total_orders")
        );
    }
}

