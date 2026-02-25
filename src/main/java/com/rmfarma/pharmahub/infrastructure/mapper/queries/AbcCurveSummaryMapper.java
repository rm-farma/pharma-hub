package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.AbcCurveSummaryDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("abc-curve-summary")
public class AbcCurveSummaryMapper implements ResultSetMapper<AbcCurveSummaryDTO> {

    @Override
    public AbcCurveSummaryDTO map(ResultSet rs) throws SQLException {
        return new AbcCurveSummaryDTO(
                rs.getLong("total_produtos"),
                rs.getLong("total_produtos_a"),
                rs.getLong("total_produtos_b"),
                rs.getLong("total_produtos_c"),
                rs.getBigDecimal("faturamento_total"),
                rs.getBigDecimal("faturamento_a"),
                rs.getBigDecimal("faturamento_b"),
                rs.getBigDecimal("faturamento_c")
        );
    }
}

