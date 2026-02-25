package com.rmfarma.pharmahub.infrastructure.db;

import com.rmfarma.pharmahub.core.model.PagedResult;
import com.rmfarma.pharmahub.core.model.QueryDefinition;
import com.rmfarma.pharmahub.core.model.UnpagedResult;
import com.rmfarma.pharmahub.core.port.QueryExecutor;
import com.rmfarma.pharmahub.infrastructure.mapper.GenericMapMapper;
import com.rmfarma.pharmahub.infrastructure.mapper.ResultSetMapper;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class JdbcQueryExecutor implements QueryExecutor {

    private static final Logger LOG = Logger.getLogger(JdbcQueryExecutor.class);

    private final AgroalDataSource dataSource;
    private final NamedParamResolver paramResolver;
    private final Instance<ResultSetMapper<?>> mappers;
    private final GenericMapMapper genericMapMapper;

    public JdbcQueryExecutor(AgroalDataSource dataSource,
                             NamedParamResolver paramResolver,
                             @Any Instance<ResultSetMapper<?>> mappers,
                             GenericMapMapper genericMapMapper) {
        this.dataSource = dataSource;
        this.paramResolver = paramResolver;
        this.mappers = mappers;
        this.genericMapMapper = genericMapMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> PagedResult<T> executePaged(QueryDefinition definition, Map<String, Object> params, int page, int pageSize) {
        String sql = definition.sqlTemplate();
        int offset = page * pageSize;

        // 1. COUNT query via subquery wrapping
        long totalItems = executeCount(sql, params, definition);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // 2. Data query com LIMIT/OFFSET
        String dataSql = appendLimitOffset(sql, pageSize, offset);
        NamedParamResolver.ResolvedQuery resolved = paramResolver.resolve(dataSql, params, definition);
        ResultSetMapper<T> mapper = (ResultSetMapper<T>) resolveMapper(definition.key());

        List<T> items = executeQuery(resolved, mapper, definition.timeoutMs());

        return new PagedResult<>(items, page, pageSize, totalItems, totalPages);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> UnpagedResult<T> executeUnpaged(QueryDefinition definition, Map<String, Object> params, int maxRows) {
        String sql = definition.sqlTemplate();
        int fetchSize = maxRows + 1;

        sql = appendLimit(sql, fetchSize);

        NamedParamResolver.ResolvedQuery resolved = paramResolver.resolve(sql, params, definition);
        ResultSetMapper<T> mapper = (ResultSetMapper<T>) resolveMapper(definition.key());

        List<T> items = executeQuery(resolved, mapper, definition.timeoutMs());

        boolean truncated = items.size() > maxRows;
        if (truncated) {
            items = new ArrayList<>(items.subList(0, maxRows));
        }

        return new UnpagedResult<>(items, items.size(), truncated);
    }

    private long executeCount(String originalSql, Map<String, Object> params, QueryDefinition definition) {
        String countSql = "SELECT COUNT(*) AS total FROM (" + originalSql.stripTrailing() + ") AS count_query";
        NamedParamResolver.ResolvedQuery resolved = paramResolver.resolve(countSql, params, definition);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(resolved.sql())) {

            int timeoutSec = Math.max(1, (int) (definition.timeoutMs() / 1000));
            ps.setQueryTimeout(timeoutSec);

            paramResolver.bind(ps, resolved.bindings());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total");
                }
            }
        } catch (SQLException e) {
            LOG.error("Erro ao executar COUNT query", e);
            throw new RuntimeException("Erro ao executar COUNT query: " + e.getMessage(), e);
        }

        return 0;
    }

    private <T> List<T> executeQuery(NamedParamResolver.ResolvedQuery resolved, ResultSetMapper<T> mapper, long timeoutMs) {
        List<T> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(resolved.sql())) {

            int timeoutSec = Math.max(1, (int) (timeoutMs / 1000));
            ps.setQueryTimeout(timeoutSec);

            paramResolver.bind(ps, resolved.bindings());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            LOG.error("Erro ao executar query SQL", e);
            throw new RuntimeException("Erro ao executar query: " + e.getMessage(), e);
        }

        return results;
    }

    private ResultSetMapper<?> resolveMapper(String queryKey) {
        for (ResultSetMapper<?> mapper : mappers) {
            Named named = mapper.getClass().getAnnotation(Named.class);
            if (named != null && named.value().equals(queryKey)) {
                return mapper;
            }
        }
        LOG.debugv("Mapper específico não encontrado para '{0}', usando GenericMapMapper", queryKey);
        return genericMapMapper;
    }

    private String appendLimitOffset(String sql, int limit, int offset) {
        return sql.stripTrailing() + "\nLIMIT " + limit + " OFFSET " + offset;
    }

    private String appendLimit(String sql, int limit) {
        return sql.stripTrailing() + "\nLIMIT " + limit;
    }
}

