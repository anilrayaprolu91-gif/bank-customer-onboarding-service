package com.bank.onboarding.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public final class EnvironmentConfigResolver {

    private EnvironmentConfigResolver() {
    }

    public static EnvironmentConfig resolve(ApiProfile profile) {
        Properties properties = load(profile);
        return new EnvironmentConfig(
                profile,
                properties.getProperty("base.url", ""),
                properties.getProperty("db.name", "onboarding_db"),
                properties.getProperty("db.username", "onboarding_user"),
                properties.getProperty("db.password", "onboarding_pass")
        );
    }

    private static Properties load(ApiProfile profile) {
        Properties properties = new Properties();
        String resource = "environments/" + profile.name().toLowerCase() + ".properties";
        try (InputStream inputStream = EnvironmentConfigResolver.class.getClassLoader().getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing environment profile: " + resource);
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to load environment profile " + resource, exception);
        }
    }
}

