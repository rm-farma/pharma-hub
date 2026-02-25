package com.rmfarma.pharmahub.infrastructure.config;

import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping(prefix = "queryhub.security")
public interface ApiKeyConfig {

    Map<String, String> apiKeys();
}

