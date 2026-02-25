package com.rmfarma.pharmahub.infrastructure.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "queryhub")
public interface QueryHubConfig {

    @WithDefault("20")
    int defaultPageSize();

    @WithDefault("100")
    int maxPageSize();

    @WithDefault("30000")
    long defaultTimeoutMs();
}

