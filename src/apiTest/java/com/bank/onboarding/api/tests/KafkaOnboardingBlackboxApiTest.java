package com.bank.onboarding.api.tests;

import com.bank.onboarding.BankCustomerOnboardingApplication;
import com.bank.onboarding.api.factory.TestDataFactory;
import com.bank.onboarding.api.model.request.CustomerOnboardingRequest;
import com.bank.onboarding.api.model.response.CustomerResponse;
import com.bank.onboarding.api.support.JsonSupport;
import com.bank.onboarding.event.ConsumedOnboardingEventStore;
import com.bank.onboarding.event.CustomerOnboardedEvent;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BankCustomerOnboardingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class KafkaOnboardingBlackboxApiTest {

    @Container
    static KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("app.kafka.enabled", () -> "true");
        registry.add("app.kafka.customer-onboarded-topic", () -> "customer.onboarded.v1");
        registry.add("app.kafka.consumer-group-id", () -> "blackbox-kafka-" + UUID.randomUUID());
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ConsumedOnboardingEventStore consumedOnboardingEventStore;

    @BeforeEach
    void clearConsumedEvents() {
        consumedOnboardingEventStore.clear();
    }

    @Test
    void shouldOnboardCustomerAndConsumeKafkaEventInSingleFlow() {
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
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    Optional<CustomerOnboardedEvent> matchedEvent = consumedOnboardingEventStore.snapshot().stream()
                            .filter(event -> event.customerId().equals(customer.id().toString()))
                            .findFirst();

                    assertThat(matchedEvent).isPresent();
                    assertThat(matchedEvent.orElseThrow().customerNumber()).isEqualTo(customer.customerNumber());
                    assertThat(matchedEvent.orElseThrow().email()).isEqualTo(customer.email());
                });
    }
}

