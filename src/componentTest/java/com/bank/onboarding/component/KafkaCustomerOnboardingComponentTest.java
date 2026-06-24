package com.bank.onboarding.component;

import com.bank.onboarding.BankCustomerOnboardingApplication;
import com.bank.onboarding.domain.enums.AddressType;
import com.bank.onboarding.domain.enums.DocumentType;
import com.bank.onboarding.domain.enums.RiskLevel;
import com.bank.onboarding.dto.request.AddressRequest;
import com.bank.onboarding.dto.request.CustomerOnboardingRequest;
import com.bank.onboarding.dto.request.KycDocumentRequest;
import com.bank.onboarding.dto.request.PersonalInfoRequest;
import com.bank.onboarding.dto.request.RiskFactorRequest;
import com.bank.onboarding.dto.request.RiskProfileRequest;
import com.bank.onboarding.dto.response.CustomerResponse;
import com.bank.onboarding.event.ConsumedOnboardingEventStore;
import com.bank.onboarding.event.CustomerOnboardedEvent;
import com.bank.onboarding.service.CustomerService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BankCustomerOnboardingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Testcontainers
class KafkaCustomerOnboardingComponentTest {

    @Container
    static KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("app.kafka.enabled", () -> "true");
        registry.add("app.kafka.customer-onboarded-topic", () -> "customer.onboarded.v1");
        registry.add("app.kafka.consumer-group-id", () -> "component-kafka-" + UUID.randomUUID());
    }

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ConsumedOnboardingEventStore consumedOnboardingEventStore;

    @BeforeEach
    void clearEvents() {
        consumedOnboardingEventStore.clear();
    }

    @Test
    void shouldProduceAndConsumeCustomerOnboardedEvent() {
        CustomerOnboardingRequest request = buildRequest();

        CustomerResponse response = customerService.onboardCustomer(request);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    List<CustomerOnboardedEvent> events = consumedOnboardingEventStore.snapshot();
                    assertThat(events).isNotEmpty();
                    CustomerOnboardedEvent latest = events.get(events.size() - 1);
                    assertThat(latest.customerId()).isEqualTo(response.getId().toString());
                    assertThat(latest.customerNumber()).isEqualTo(response.getCustomerNumber());
                    assertThat(latest.email()).isEqualTo(request.getPersonalInfo().getEmail());
                    assertThat(latest.occurredAt()).isBeforeOrEqualTo(Instant.now());
                });
    }

    private CustomerOnboardingRequest buildRequest() {
        String uniqueToken = UUID.randomUUID().toString().substring(0, 8);

        PersonalInfoRequest personalInfo = PersonalInfoRequest.builder()
                .firstName("Kafka")
                .lastName("Demo")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality("US")
                .taxIdentificationNumber("TIN-" + uniqueToken)
                .email("kafka-demo-" + uniqueToken + "@example.com")
                .phoneNumber("+1555000" + uniqueToken.substring(0, 4))
                .build();

        AddressRequest address = AddressRequest.builder()
                .addressType(AddressType.HOME)
                .street("123 Kafka Street")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("US")
                .isPrimary(true)
                .build();

        KycDocumentRequest kyc = KycDocumentRequest.builder()
                .documentType(DocumentType.PASSPORT)
                .documentNumber("P-" + uniqueToken)
                .issuingAuthority("US Gov")
                .issuingCountry("US")
                .issueDate(LocalDate.now().minusYears(5))
                .expiryDate(LocalDate.now().plusYears(5))
                .build();

        RiskFactorRequest factor = RiskFactorRequest.builder()
                .factorName("LOW_RISK_COUNTRY")
                .factorDescription("Customer domicile is in a low risk jurisdiction")
                .weight(new BigDecimal("0.20"))
                .build();

        RiskProfileRequest riskProfile = RiskProfileRequest.builder()
                .riskLevel(RiskLevel.LOW)
                .riskScore(20)
                .assessedBy("component-test")
                .assessmentNotes("Kafka integration verification")
                .factors(List.of(factor))
                .build();

        return CustomerOnboardingRequest.builder()
                .personalInfo(personalInfo)
                .addresses(List.of(address))
                .kycDocuments(List.of(kyc))
                .riskProfile(riskProfile)
                .build();
    }
}

