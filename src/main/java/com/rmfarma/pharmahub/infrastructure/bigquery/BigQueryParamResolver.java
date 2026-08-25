package com.rmfarma.pharmahub.infrastructure.bigquery;

import com.google.cloud.bigquery.QueryParameterValue;
import com.rmfarma.pharmahub.core.model.ParamDefinition;
import com.rmfarma.pharmahub.core.model.ParamType;
import com.rmfarma.pharmahub.core.model.QueryDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class BigQueryParamResolver {

    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile("@(\\w+)");

    public Map<String, QueryParameterValue> resolve(String sql, Map<String, Object> params, QueryDefinition definition) {
        Map<String, ParamDefinition> paramDefs = new HashMap<>();
        for (ParamDefinition pd : definition.params()) {
            paramDefs.put(pd.name(), pd);
        }

        Map<String, QueryParameterValue> bindings = new HashMap<>();
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(sql);
        while (matcher.find()) {
            String paramName = matcher.group(1);
            ParamDefinition paramDef = paramDefs.get(paramName);
            ParamType type = paramDef != null ? paramDef.type() : ParamType.STRING;
            Object value = params.get(paramName);
            bindings.put(paramName, type.toQueryParameterValue(value));
        }
        return bindings;
    }
}
