package com.rmfarma.pharmahub.infrastructure.mapper;

import com.rmfarma.pharmahub.core.model.QueryDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolve o RowMapper de uma query.
 *
 * <p>Não dá para inspecionar as anotações via {@code instancia.getClass()}: beans
 * normal-scoped chegam aqui como client proxy do ArC, uma subclasse gerada que não herda
 * o {@code @Named} do bean original (anotações de classe não são {@code @Inherited}).
 * Era isso que fazia todas as queries caírem no {@link GenericMapMapper} e devolverem
 * {@code field_0, field_1, ...} em vez das colunas reais.
 *
 * <p>A resolução usa os metadados do bean no container, na ordem:
 * <ol>
 *   <li>{@code mapperClassName} declarado no metadata.yaml da query;</li>
 *   <li>qualifier {@code @Named} igual à key da query;</li>
 *   <li>fallback genérico (logado como WARN — fallback silencioso foi a causa do bug).</li>
 * </ol>
 */
@ApplicationScoped
public class MapperResolver {

    private static final Logger LOG = Logger.getLogger(MapperResolver.class);

    private final Instance<RowMapper<?>> mappers;
    private final GenericMapMapper genericMapMapper;
    private final Map<String, RowMapper<?>> cache = new ConcurrentHashMap<>();

    public MapperResolver(@Any Instance<RowMapper<?>> mappers, GenericMapMapper genericMapMapper) {
        this.mappers = mappers;
        this.genericMapMapper = genericMapMapper;
    }

    public RowMapper<?> resolve(QueryDefinition definition) {
        return cache.computeIfAbsent(definition.key(), key -> {
            RowMapper<?> mapper = byClassName(definition.mapperClassName());
            if (mapper == null) {
                mapper = byQualifier(key);
            }
            if (mapper == null) {
                LOG.warnv("Nenhum mapper específico encontrado para a query ''{0}'' "
                        + "(mapperClassName={1}); usando GenericMapMapper.",
                        key, definition.mapperClassName());
                return genericMapMapper;
            }
            LOG.debugv("Query ''{0}'' resolvida para o mapper {1}", key, mapper.getClass().getName());
            return mapper;
        });
    }

    private RowMapper<?> byClassName(String mapperClassName) {
        if (mapperClassName == null || mapperClassName.isBlank()) {
            return null;
        }
        for (Instance.Handle<RowMapper<?>> handle : mappers.handles()) {
            if (mapperClassName.equals(handle.getBean().getBeanClass().getName())) {
                return handle.get();
            }
        }
        return null;
    }

    private RowMapper<?> byQualifier(String queryKey) {
        Instance<RowMapper<?>> selected = mappers.select(NamedLiteral.of(queryKey));
        return selected.isResolvable() ? selected.get() : null;
    }
}
