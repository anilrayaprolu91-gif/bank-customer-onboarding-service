package com.bank.onboarding.api.config;

import java.util.Locale;

public enum ApiProfile {
    EMBEDDED,
    LOCAL,
    CONTAINER,
    MOCK;

    public static ApiProfile from(String value) {
        if (value == null || value.isBlank()) {
            return EMBEDDED;
        }
        return ApiProfile.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}

