package com.bank.onboarding.consumercontract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "bank-customer-onboarding-provider", pactVersion = PactSpecVersion.V3)
class CustomerOnboardingConsumerPactTest {

    @Pact(consumer = "digital-onboarding-portal")
    RequestResponsePact shouldOnboardCustomerContract(au.com.dius.pact.consumer.dsl.PactDslWithProvider builder) {
        DslPart requestBody = LambdaDsl.newJsonBody((body) -> {
            body.object("personalInfo", personal -> {
                personal.stringType("firstName", "Jane");
                personal.stringType("lastName", "Smith");
                personal.stringType("dateOfBirth", "1992-04-18");
                personal.stringType("nationality", "US");
                personal.stringType("taxIdentificationNumber", "987-65-1234");
                personal.stringType("email", "consumer.contract@example.com");
                personal.stringType("phoneNumber", "+15551234567");
            });
            body.array("addresses", addresses -> addresses.object(address -> {
                address.stringType("addressType", "HOME");
                address.stringType("street", "789 Elm Street");
                address.stringType("city", "Chicago");
                address.stringType("state", "IL");
                address.stringType("postalCode", "60601");
                address.stringType("country", "US");
                address.booleanType("isPrimary", true);
            }));
            body.array("kycDocuments", docs -> docs.object(doc -> {
                doc.stringType("documentType", "PASSPORT");
                doc.stringType("documentNumber", "P12345678");
                doc.stringType("issuingAuthority", "U.S. Department of State");
                doc.stringType("issuingCountry", "US");
                doc.stringType("issueDate", "2018-01-15");
                doc.stringType("expiryDate", "2030-01-15");
            }));
            body.object("riskProfile", risk -> {
                risk.stringType("riskLevel", "LOW");
                risk.numberType("riskScore", 20);
                risk.stringType("assessedBy", "AUTO_SCREENING_v2.1");
            });
            body.object("initialAccount", account -> {
                account.stringType("accountType", "CHECKING");
                account.stringType("currency", "USD");
                account.numberValue("initialDeposit", BigDecimal.valueOf(1000.00));
                account.stringType("productCode", "CHK-STANDARD");
            });
        }).build();

        DslPart responseBody = LambdaDsl.newJsonBody((body) -> {
            body.booleanType("success", true);
            body.stringType("message", "Customer onboarded successfully");
            body.object("data", data -> {
                data.uuid("id");
                data.stringMatcher("customerNumber", "CUST-.*", "CUST-1234-20260101");
                data.stringType("email", "consumer.contract@example.com");
                data.stringType("customerStatus", "PENDING_VERIFICATION");
            });
            body.stringMatcher("timestamp", "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}", "2026-01-01T10:30:00");
        }).build();

        return builder
                .given("customer email does not already exist")
                .uponReceiving("a valid onboarding request")
                .path("/api/v1/customers/onboard")
                .method("POST")
                .headers("Content-Type", "application/json", "Accept", "application/json")
                .body(requestBody)
                .willRespondWith()
                .status(201)
                .headers(Map.of("Content-Type", "application/json"))
                .body(responseBody)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "shouldOnboardCustomerContract")
    void shouldMatchConsumerContract(MockServer mockServer) throws IOException, InterruptedException {
        String requestJson = """
                {
                  \"personalInfo\": {
                    \"firstName\": \"Jane\",
                    \"lastName\": \"Smith\",
                    \"dateOfBirth\": \"1992-04-18\",
                    \"nationality\": \"US\",
                    \"taxIdentificationNumber\": \"987-65-1234\",
                    \"email\": \"consumer.contract@example.com\",
                    \"phoneNumber\": \"+15551234567\"
                  },
                  \"addresses\": [
                    {
                      \"addressType\": \"HOME\",
                      \"street\": \"789 Elm Street\",
                      \"city\": \"Chicago\",
                      \"state\": \"IL\",
                      \"postalCode\": \"60601\",
                      \"country\": \"US\",
                      \"isPrimary\": true
                    }
                  ],
                  \"kycDocuments\": [
                    {
                      \"documentType\": \"PASSPORT\",
                      \"documentNumber\": \"P12345678\",
                      \"issuingAuthority\": \"U.S. Department of State\",
                      \"issuingCountry\": \"US\",
                      \"issueDate\": \"2018-01-15\",
                      \"expiryDate\": \"2030-01-15\"
                    }
                  ],
                  \"riskProfile\": {
                    \"riskLevel\": \"LOW\",
                    \"riskScore\": 20,
                    \"assessedBy\": \"AUTO_SCREENING_v2.1\"
                  },
                  \"initialAccount\": {
                    \"accountType\": \"CHECKING\",
                    \"currency\": \"USD\",
                    \"initialDeposit\": 1000.00,
                    \"productCode\": \"CHK-STANDARD\"
                  }
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mockServer.getUrl() + "/api/v1/customers/onboard"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"success\":true");
        assertThat(response.body()).contains("\"message\":\"Customer onboarded successfully\"");
        assertThat(response.body()).contains("\"customerStatus\":\"PENDING_VERIFICATION\"");
    }
}

