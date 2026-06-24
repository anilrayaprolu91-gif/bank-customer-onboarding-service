package com.bank.onboarding.api.tests;

import com.bank.onboarding.api.factory.RequestSpecificationFactory;
import com.bank.onboarding.api.support.WireMockSupport;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Automation framework utilities")
@Feature("WireMock support")
class WireMockSupportTest {

    @Test
    @Description("Verifies the framework can stand up a WireMock dependency stub and validate schema contracts.")
    void shouldStubRiskReferenceService() {
        try (WireMockSupport wireMockSupport = new WireMockSupport()) {
            wireMockSupport.start();
            wireMockSupport.stubRiskCountry("US", "LOW", 12);

            Response response = RestAssured.given(RequestSpecificationFactory.create(wireMockSupport.baseUrl()))
                    .when()
                    .get("/reference/risk-countries/US")
                    .andReturn();

            response.then()
                    .statusCode(200)
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/wiremock-risk-response-schema.json"));

            assertThat(response.jsonPath().getString("country")).isEqualTo("US");
            assertThat(response.jsonPath().getString("riskLevel")).isEqualTo("LOW");
            assertThat(response.jsonPath().getInt("riskScore")).isEqualTo(12);
        }
    }
}

