package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.TopProductDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("top-products")
public class TopProductMapper implements ResultSetMapper<TopProductDTO> {

    @Override
    public TopProductDTO map(ResultSet rs) throws SQLException {
        return new TopProductDTO(
                rs.getString("product_name"),
                rs.getBigDecimal("total_quantity"),
                rs.getBigDecimal("total_amount")
        );
    }
}

