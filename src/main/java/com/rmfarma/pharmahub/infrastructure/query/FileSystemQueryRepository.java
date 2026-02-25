package com.rmfarma.pharmahub.infrastructure.query;

import com.rmfarma.pharmahub.core.model.ParamDefinition;
import com.rmfarma.pharmahub.core.model.ParamType;
import com.rmfarma.pharmahub.core.model.QueryDefinition;
import com.rmfarma.pharmahub.core.port.QueryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ApplicationScoped
public class FileSystemQueryRepository implements QueryRepository {

    private static final Logger LOG = Logger.getLogger(FileSystemQueryRepository.class);
    private static final String QUERIES_BASE_PATH = "queries/";
    private static final String[] QUERY_KEYS = {
            "sales-summary", "top-sellers", "top-products", "sales-comparison",
            "stock-without-sales", "stock-search", "stock-metrics",
            "abc-curve-summary", "abc-curve-products", "sales-overview", "idle-stock"
    };

    private final Map<String, QueryDefinition> queries = new LinkedHashMap<>();

    @PostConstruct
    void init() {
        Yaml yaml = new Yaml();

        for (String key : QUERY_KEYS) {
            String metadataPath = QUERIES_BASE_PATH + key + "/metadata.yaml";
            String sqlPath = QUERIES_BASE_PATH + key + "/query.sql";

            try (InputStream metaStream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(metadataPath);
                 InputStream sqlStream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(sqlPath)) {

                if (metaStream == null || sqlStream == null) {
                    LOG.warnv("Query '{0}' ignorada: metadata ou SQL não encontrado", key);
                    continue;
                }

                Map<String, Object> meta = yaml.load(metaStream);
                String sqlTemplate = new String(sqlStream.readAllBytes(), StandardCharsets.UTF_8);

                QueryDefinition definition = parseDefinition(key, meta, sqlTemplate);
                queries.put(key, definition);

                LOG.infov("Query carregada: {0} (v{1})", key, definition.version());

            } catch (IOException e) {
                LOG.errorv(e, "Erro ao carregar query '{0}'", key);
            }
        }

        LOG.infov("Total de queries carregadas: {0}", queries.size());
    }

    @Override
    public Optional<QueryDefinition> findByKey(String key) {
        return Optional.ofNullable(queries.get(key));
    }

    @Override
    public List<QueryDefinition> findAll() {
        return List.copyOf(queries.values());
    }

    @SuppressWarnings("unchecked")
    private QueryDefinition parseDefinition(String key, Map<String, Object> meta, String sqlTemplate) {
        String version = getString(meta, "version", "1.0.0");
        String description = getString(meta, "description", "");
        String endpoint = getString(meta, "endpoint", "/queries/" + key + "/execute");
        List<String> tags = getList(meta, "tags");

        Map<String, Object> paginationMap = (Map<String, Object>) meta.getOrDefault("pagination", Map.of());
        int defaultPageSize = getInt(paginationMap, "defaultPageSize", 20);
        int maxPageSize = getInt(paginationMap, "maxPageSize", 100);
        boolean allowUnpaged = getBoolean(paginationMap, "allowUnpaged", false);
        int maxUnpagedRows = getInt(paginationMap, "maxUnpagedRows", 1000);
        long timeoutMs = getLong(meta, "timeoutMs", 30000L);

        String dtoClassName = getString(meta, "dtoClassName", "");
        String mapperClassName = getString(meta, "mapperClassName", "");

        List<Map<String, Object>> paramsList = (List<Map<String, Object>>) meta.getOrDefault("params", List.of());
        List<ParamDefinition> params = paramsList.stream()
                .map(this::parseParam)
                .toList();

        return new QueryDefinition(
                key, version, description, endpoint, tags, sqlTemplate,
                params, defaultPageSize, maxPageSize, allowUnpaged,
                maxUnpagedRows, timeoutMs, dtoClassName, mapperClassName
        );
    }

    private ParamDefinition parseParam(Map<String, Object> paramMap) {
        String name = getString(paramMap, "name", "");
        ParamType type = ParamType.fromString(getString(paramMap, "type", "STRING"));
        boolean required = getBoolean(paramMap, "required", false);
        String description = getString(paramMap, "description", "");
        String defaultValue = paramMap.containsKey("defaultValue")
                ? String.valueOf(paramMap.get("defaultValue"))
                : null;
        return new ParamDefinition(name, type, required, description, defaultValue);
    }

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) return Integer.parseInt(s);
        return defaultValue;
    }

    private long getLong(Map<String, Object> map, String key, long defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        return defaultValue;
    }

    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private List<String> getList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }
}

