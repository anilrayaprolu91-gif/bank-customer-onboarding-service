package com.bank.onboarding.api.config;

public record EnvironmentConfig(
        ApiProfile profile,
        String baseUrl,
        String dbName,
        String dbUsername,
        String dbPassword
) {
    public EnvironmentConfig withBaseUrl(String resolvedBaseUrl) {
        return new EnvironmentConfig(profile, resolvedBaseUrl, dbName, dbUsername, dbPassword);
    }
}

