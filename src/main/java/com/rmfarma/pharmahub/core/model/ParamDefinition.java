package com.rmfarma.pharmahub.core.model;

public record ParamDefinition(
        String name,
        ParamType type,
        boolean required,
        String description,
        String defaultValue
) {
}

