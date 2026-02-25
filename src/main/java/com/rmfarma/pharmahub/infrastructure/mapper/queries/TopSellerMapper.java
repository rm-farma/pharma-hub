package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.TopSellerDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("top-sellers")
public class TopSellerMapper implements ResultSetMapper<TopSellerDTO> {

    @Override
    public TopSellerDTO map(ResultSet rs) throws SQLException {
        return new TopSellerDTO(
                rs.getString("seller"),
                rs.getBigDecimal("total_amount"),
                rs.getLong("total_orders")
        );
    }
}

