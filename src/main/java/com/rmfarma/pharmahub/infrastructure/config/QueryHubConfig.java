package com.rmfarma.pharmahub.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class QueryHubConfig {

    @ConfigProperty(name = "queryhub.default-page-size", defaultValue = "20")
    int defaultPageSize;

    @ConfigProperty(name = "queryhub.max-page-size", defaultValue = "100")
    int maxPageSize;

    @ConfigProperty(name = "queryhub.default-timeout-ms", defaultValue = "30000")
    long defaultTimeoutMs;

    public int defaultPageSize() {
        return defaultPageSize;
    }

    public int maxPageSize() {
        return maxPageSize;
    }

    public long defaultTimeoutMs() {
        return defaultTimeoutMs;
    }
}

