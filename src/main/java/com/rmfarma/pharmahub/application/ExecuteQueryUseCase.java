package com.rmfarma.pharmahub.application;

import com.rmfarma.pharmahub.core.exception.ParamValidationException;
import com.rmfarma.pharmahub.core.exception.QueryNotFoundException;
import com.rmfarma.pharmahub.core.exception.UnpagedNotAllowedException;
import com.rmfarma.pharmahub.core.model.*;
import com.rmfarma.pharmahub.core.port.QueryExecutor;
import com.rmfarma.pharmahub.core.port.QueryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ExecuteQueryUseCase {

    private static final Logger LOG = Logger.getLogger(ExecuteQueryUseCase.class);

    private final QueryRepository queryRepository;
    private final QueryExecutor queryExecutor;

    public ExecuteQueryUseCase(QueryRepository queryRepository, QueryExecutor queryExecutor) {
        this.queryRepository = queryRepository;
        this.queryExecutor = queryExecutor;
    }

    public record ExecutionResult(
            ExecutionMode mode,
            Object result,
            QueryDefinition definition
    ) {
    }

    public ExecutionResult execute(String queryKey, Map<String, Object> rawParams,
                                   Integer page, Integer pageSize, Boolean unpaged) {
        QueryDefinition definition = queryRepository.findByKey(queryKey)
                .orElseThrow(() -> new QueryNotFoundException(queryKey));

        Map<String, Object> resolvedParams = validateAndResolveParams(definition, rawParams);

        ExecutionMode mode = determineMode(definition, unpaged);

        if (mode == ExecutionMode.UNPAGED) {
            if (!definition.allowUnpaged()) {
                throw new UnpagedNotAllowedException(queryKey);
            }
            UnpagedResult<?> result = queryExecutor.executeUnpaged(
                    definition, resolvedParams, definition.maxUnpagedRows());
            return new ExecutionResult(mode, result, definition);
        } else {
            int resolvedPage = (page != null && page >= 1) ? page : 1;
            int resolvedPageSize = resolvePageSize(definition, pageSize);
            PagedResult<?> result = queryExecutor.executePaged(
                    definition, resolvedParams, resolvedPage, resolvedPageSize);
            return new ExecutionResult(mode, result, definition);
        }
    }

    private ExecutionMode determineMode(QueryDefinition definition, Boolean unpaged) {
        if (Boolean.TRUE.equals(unpaged)) {
            return ExecutionMode.UNPAGED;
        }
        // Se defaultPageSize <= 0 e allowUnpaged, modo unpaged por padrão (queries de 1 row)
        if (definition.defaultPageSize() <= 1 && definition.allowUnpaged() && definition.maxUnpagedRows() <= 1) {
            return ExecutionMode.UNPAGED;
        }
        return ExecutionMode.PAGED;
    }

    private int resolvePageSize(QueryDefinition definition, Integer requestedPageSize) {
        if (requestedPageSize != null && requestedPageSize > 0) {
            return Math.min(requestedPageSize, definition.maxPageSize());
        }
        return definition.defaultPageSize();
    }

    private Map<String, Object> validateAndResolveParams(QueryDefinition definition, Map<String, Object> rawParams) {
        Map<String, Object> resolved = new HashMap<>(rawParams != null ? rawParams : Map.of());

        for (ParamDefinition paramDef : definition.params()) {
            Object value = resolved.get(paramDef.name());

            if (value == null || (value instanceof String s && s.isBlank())) {
                if (paramDef.required()) {
                    throw new ParamValidationException(
                            "Parâmetro obrigatório ausente: " + paramDef.name());
                }
                if (paramDef.defaultValue() != null) {
                    resolved.put(paramDef.name(), paramDef.type().convert(paramDef.defaultValue()));
                }
            } else {
                try {
                    if (value instanceof String s) {
                        resolved.put(paramDef.name(), paramDef.type().convert(s));
                    } else if (value instanceof Number n && paramDef.type() == ParamType.INTEGER) {
                        resolved.put(paramDef.name(), n.intValue());
                    } else if (value instanceof Number n && paramDef.type() == ParamType.LONG) {
                        resolved.put(paramDef.name(), n.longValue());
                    }
                } catch (Exception e) {
                    throw new ParamValidationException(
                            "Parâmetro '%s' com valor inválido para tipo %s: %s"
                                    .formatted(paramDef.name(), paramDef.type(), value));
                }
            }
        }

        return resolved;
    }
}

