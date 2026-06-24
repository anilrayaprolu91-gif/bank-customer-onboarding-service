package com.bank.onboarding.api.tests;

import com.bank.onboarding.BankCustomerOnboardingApplication;
import com.bank.onboarding.api.factory.TestDataFactory;
import com.bank.onboarding.api.model.request.CustomerOnboardingRequest;
import com.bank.onboarding.api.model.response.CustomerResponse;
import com.bank.onboarding.api.support.JsonSupport;
import com.bank.onboarding.event.ConsumedOnboardingEventStore;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BankCustomerOnboardingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.kafka.enabled=false")
class KafkaDisabledOnboardingBlackboxApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ConsumedOnboardingEventStore consumedOnboardingEventStore;

    @BeforeEach
    void clearConsumedEvents() {
        consumedOnboardingEventStore.clear();
    }

    @Test
    void shouldOnboardCustomerSuccessfullyWhenKafkaDisabledAndEmitNoConsumedEvent() {
        CustomerOnboardingRequest onboardingRequest = TestDataFactory.newCustomerOnboardingRequest();

        Response onboardResponse = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(onboardingRequest)
                .when()
                .post("/api/v1/customers/onboard")
                .andReturn();

        onboardResponse.then()
                .statusCode(201)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/customer-onboard-response-schema.json"));

        CustomerResponse customer = JsonSupport.readData(onboardResponse, CustomerResponse.class);
        assertThat(customer.id()).isNotNull();
        assertThat(customer.email()).isEqualTo(onboardingRequest.personalInfo().email());
        assertThat(customer.customerStatus()).isEqualTo("PENDING_VERIFICATION");

        Awaitility.await()
                .during(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> assertThat(consumedOnboardingEventStore.snapshot()).isEmpty());
    }
}

