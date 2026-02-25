package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.IdleStockDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("idle-stock")
public class IdleStockMapper implements ResultSetMapper<IdleStockDTO> {

    @Override
    public IdleStockDTO map(ResultSet rs) throws SQLException {
        return new IdleStockDTO(
                rs.getString("ean"),
                rs.getString("apresentacao"),
                rs.getString("fabricante"),
                rs.getString("grupo_macro"),
                rs.getBigDecimal("saldo_estoque"),
                rs.getBigDecimal("custo_medio_total"),
                rs.getBigDecimal("preco_venda"),
                rs.getString("total_skus"),
                rs.getString("total_unidades"),
                rs.getString("valor_total_custo"),
                rs.getString("valor_total_venda")
        );
    }
}

