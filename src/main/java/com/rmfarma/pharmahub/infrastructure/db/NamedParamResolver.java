package com.rmfarma.pharmahub.infrastructure.db;

import com.rmfarma.pharmahub.core.model.ParamDefinition;
import com.rmfarma.pharmahub.core.model.ParamType;
import com.rmfarma.pharmahub.core.model.QueryDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class NamedParamResolver {

    // Captura :param mas ignora ::cast do PostgreSQL (ex: ::numeric, ::text)
    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile("(?<!:):(?!:)(\\w+)");

    public record ResolvedQuery(String sql, List<ParamBinding> bindings) {
    }

    public record ParamBinding(String name, ParamType type, Object value) {
    }

    public ResolvedQuery resolve(String sqlTemplate, Map<String, Object> params, QueryDefinition definition) {
        Map<String, ParamDefinition> paramDefs = new java.util.HashMap<>();
        for (ParamDefinition pd : definition.params()) {
            paramDefs.put(pd.name(), pd);
        }

        List<ParamBinding> bindings = new ArrayList<>();
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(sqlTemplate);
        StringBuilder resolved = new StringBuilder();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            ParamDefinition paramDef = paramDefs.get(paramName);
            ParamType type = paramDef != null ? paramDef.type() : ParamType.STRING;
            Object value = params.get(paramName);

            bindings.add(new ParamBinding(paramName, type, value));
            matcher.appendReplacement(resolved, "?");
        }
        matcher.appendTail(resolved);

        return new ResolvedQuery(resolved.toString(), bindings);
    }

    public void bind(PreparedStatement ps, List<ParamBinding> bindings) throws SQLException {
        for (int i = 0; i < bindings.size(); i++) {
            ParamBinding binding = bindings.get(i);
            binding.type().bind(ps, i + 1, binding.value());
        }
    }
}

