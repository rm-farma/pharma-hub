package com.rmfarma.pharmahub.infrastructure.mapper.queries;

import com.rmfarma.pharmahub.api.dto.response.queries.AbcCurveProductDTO;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
@Named("abc-curve-products")
public class AbcCurveProductMapper implements ResultSetMapper<AbcCurveProductDTO> {

    @Override
    public AbcCurveProductDTO map(ResultSet rs) throws SQLException {
        return new AbcCurveProductDTO(
                rs.getString("ean"),
                rs.getString("apresentacao"),
                rs.getBigDecimal("faturamento_total"),
                rs.getBigDecimal("quantidade_vendida"),
                rs.getLong("num_transacoes"),
                rs.getBigDecimal("percentual_individual"),
                rs.getBigDecimal("percentual_acumulado"),
                rs.getString("classe_abc"),
                rs.getBigDecimal("saldo_estoque"),
                rs.getBigDecimal("preco_venda"),
                rs.getBigDecimal("custo_medio")
        );
    }
}

