package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.SalesComparisonDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("sales-comparison")
public class SalesComparisonMapper implements ResultSetMapper<SalesComparisonDTO> {

    @Override
    public SalesComparisonDTO map(ResultSet rs) throws SQLException {
        return new SalesComparisonDTO(
                rs.getString("periodo_base"),
                rs.getString("periodo_comparado"),
                rs.getBigDecimal("faturamento_base"),
                rs.getBigDecimal("faturamento_comparado"),
                rs.getBigDecimal("variacao_faturamento"),
                rs.getBigDecimal("itens_vendidos_base"),
                rs.getBigDecimal("itens_vendidos_comparado"),
                rs.getBigDecimal("variacao_itens_vendidos"),
                rs.getBigDecimal("ticket_medio_base"),
                rs.getBigDecimal("ticket_medio_comparado"),
                rs.getBigDecimal("variacao_ticket_medio")
        );
    }
}

