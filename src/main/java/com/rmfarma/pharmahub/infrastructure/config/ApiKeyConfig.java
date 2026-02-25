package com.rmfarma.pharmahub.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

@ApplicationScoped
public class ApiKeyConfig {

    @ConfigProperty(name = "queryhub.security.api-keys")
    Map<String, String> apiKeys;

    public Map<String, String> apiKeys() {
        return apiKeys;
    }
}

