package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.StockWithoutSalesDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("stock-without-sales")
public class StockWithoutSalesMapper implements ResultSetMapper<StockWithoutSalesDTO> {

    @Override
    public StockWithoutSalesDTO map(ResultSet rs) throws SQLException {
        return new StockWithoutSalesDTO(
                rs.getString("ean"),
                rs.getString("apresentacao"),
                rs.getString("fabricante"),
                rs.getString("grupo_macro"),
                rs.getBigDecimal("saldo_estoque"),
                rs.getBigDecimal("custo_medio_total"),
                rs.getBigDecimal("preco_venda")
        );
    }
}

