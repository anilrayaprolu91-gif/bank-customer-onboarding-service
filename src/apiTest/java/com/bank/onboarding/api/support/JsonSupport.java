package com.bank.onboarding.api.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

public final class JsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonSupport() {
    }

    public static JsonNode readTree(Response response) {
        try {
            return OBJECT_MAPPER.readTree(response.asString());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to parse API response", exception);
        }
    }

    public static <T> T readData(Response response, Class<T> targetType) {
        try {
            return OBJECT_MAPPER.treeToValue(readTree(response).get("data"), targetType);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to map response data to " + targetType.getSimpleName(), exception);
        }
    }
}

