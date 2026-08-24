package com.rmfarma.pharmahub.infrastructure.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.TableResult;
import com.rmfarma.pharmahub.core.model.PagedResult;
import com.rmfarma.pharmahub.core.model.QueryDefinition;
import com.rmfarma.pharmahub.core.model.UnpagedResult;
import com.rmfarma.pharmahub.core.port.QueryExecutor;
import com.rmfarma.pharmahub.infrastructure.mapper.GenericMapMapper;
import com.rmfarma.pharmahub.infrastructure.mapper.RowMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ApplicationScoped
public class BigQueryQueryExecutor implements QueryExecutor {

    private static final Logger LOG = Logger.getLogger(BigQueryQueryExecutor.class);

    /**
     * Detecta LIMIT no final do SQL (hardcoded ou com named param @limit).
     * Ignora LIMIT dentro de subqueries — só faz match no LIMIT mais externo no final do SQL.
     */
    private static final Pattern TRAILING_LIMIT_PATTERN =
            Pattern.compile("(?i)\\bLIMIT\\s+(@\\w+|\\d+)\\s*$");

    private final BigQuery bigquery;
    private final BigQueryParamResolver paramResolver;
    private final Instance<RowMapper<?>> mappers;
    private final GenericMapMapper genericMapMapper;

    public BigQueryQueryExecutor(BigQuery bigquery,
                                  BigQueryParamResolver paramResolver,
                                  @Any Instance<RowMapper<?>> mappers,
                                  GenericMapMapper genericMapMapper) {
        this.bigquery = bigquery;
        this.paramResolver = paramResolver;
        this.mappers = mappers;
        this.genericMapMapper = genericMapMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> PagedResult<T> executePaged(QueryDefinition definition, Map<String, Object> params, int page, int pageSize) {
        String sql = definition.sqlTemplate().stripTrailing();
        int offset = (page - 1) * pageSize;

        // Para o COUNT, usamos o SQL sem LIMIT/OFFSET (removemos trailing LIMIT se existir)
        String sqlWithoutLimit = stripTrailingLimit(sql);

        // 1. COUNT query via subquery wrapping
        long totalItems = executeCount(sqlWithoutLimit, params, definition);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // 2. Data query: remover LIMIT original e usar nosso LIMIT/OFFSET
        String dataSql = sqlWithoutLimit + "\nLIMIT " + pageSize + " OFFSET " + offset;
        RowMapper<T> mapper = (RowMapper<T>) resolveMapper(definition.key());

        List<T> items = executeQuery(dataSql, params, definition, mapper);

        return new PagedResult<>(items, page, pageSize, totalItems, totalPages);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> UnpagedResult<T> executeUnpaged(QueryDefinition definition, Map<String, Object> params, int maxRows) {
        String sql = definition.sqlTemplate().stripTrailing();

        // Se o SQL já tem LIMIT (hardcoded ou @param), não adicionar outro
        if (!hasTrailingLimit(sql)) {
            sql = sql + "\nLIMIT " + (maxRows + 1);
        }

        RowMapper<T> mapper = (RowMapper<T>) resolveMapper(definition.key());
        List<T> items = executeQuery(sql, params, definition, mapper);

        boolean truncated = items.size() > maxRows;
        if (truncated) {
            items = new ArrayList<>(items.subList(0, maxRows));
        }

        return new UnpagedResult<>(items, items.size(), truncated);
    }

    private long executeCount(String sqlWithoutLimit, Map<String, Object> params, QueryDefinition definition) {
        String countSql = "SELECT COUNT(*) AS total FROM (" + sqlWithoutLimit + ") AS count_query";
        TableResult result = runJob(countSql, params, definition);
        for (FieldValueList row : result.iterateAll()) {
            return row.get("total").getLongValue();
        }
        return 0;
    }

    private <T> List<T> executeQuery(String sql, Map<String, Object> params, QueryDefinition definition, RowMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        TableResult result = runJob(sql, params, definition);
        for (FieldValueList row : result.iterateAll()) {
            results.add(mapper.map(row));
        }
        return results;
    }

    private TableResult runJob(String sql, Map<String, Object> params, QueryDefinition definition) {
        Map<String, QueryParameterValue> bindings = paramResolver.resolve(sql, params, definition);

        LOG.debugv("Executando SQL BigQuery: {0}", sql);
        LOG.debugv("Bindings: {0}", bindings);

        QueryJobConfiguration.Builder configBuilder = QueryJobConfiguration.newBuilder(sql)
                .setJobTimeoutMs(definition.timeoutMs());
        bindings.forEach(configBuilder::addNamedParameter);

        try {
            return bigquery.query(configBuilder.build());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Execução da query BigQuery interrompida: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            LOG.error("Erro ao executar query no BigQuery", e);
            throw new RuntimeException("Erro ao executar query: " + e.getMessage(), e);
        }
    }

    private RowMapper<?> resolveMapper(String queryKey) {
        for (RowMapper<?> mapper : mappers) {
            Named named = mapper.getClass().getAnnotation(Named.class);
            if (named != null && named.value().equals(queryKey)) {
                return mapper;
            }
        }
        LOG.debugv("Mapper específico não encontrado para '{0}', usando GenericMapMapper", queryKey);
        return genericMapMapper;
    }

    /**
     * Verifica se o SQL termina com LIMIT (hardcoded ou @param).
     */
    private boolean hasTrailingLimit(String sql) {
        return TRAILING_LIMIT_PATTERN.matcher(sql.stripTrailing()).find();
    }

    /**
     * Remove o LIMIT final do SQL (hardcoded ou @param), preservando o restante.
     * Usado para COUNT e para substituir pelo nosso LIMIT/OFFSET na paginação.
     */
    private String stripTrailingLimit(String sql) {
        return TRAILING_LIMIT_PATTERN.matcher(sql.stripTrailing()).replaceAll("").stripTrailing();
    }
}
