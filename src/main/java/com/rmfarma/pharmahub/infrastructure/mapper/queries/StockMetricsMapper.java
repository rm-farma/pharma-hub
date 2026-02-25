package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.StockMetricsDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("stock-metrics")
public class StockMetricsMapper implements ResultSetMapper<StockMetricsDTO> {

    @Override
    public StockMetricsDTO map(ResultSet rs) throws SQLException {
        return new StockMetricsDTO(
                rs.getString("cnpj"),
                rs.getString("grupo_economico"),
                rs.getBigDecimal("total_custo_estoque"),
                rs.getLong("total_itens_alta_rotatividade")
        );
    }
}

