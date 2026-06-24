package com.bank.onboarding.contract.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;

import java.util.Map;
import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "customer-onboarding-service", port = "0", pactVersion = PactSpecVersion.V3)
class CustomerOnboardingConsumerPactTest {

    @Pact(consumer = "customer-portal-web")
    RequestResponsePact onboardingSuccess(PactDslWithProvider builder) {
        DslPart requestBody = LambdaDsl.newJsonBody((body) -> {
            body.object("personalInfo", p -> {
                p.stringType("firstName", "Jane");
                p.stringType("lastName", "Smith");
                p.stringType("dateOfBirth", "1992-04-18");
                p.stringType("nationality", "US");
                p.stringType("taxIdentificationNumber", "987-65-1234");
                p.stringType("email", "pact.customer@example.com");
                p.stringType("phoneNumber", "+15551234567");
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
                risk.array("factors", factors -> factors.object(factor -> {
                    factor.stringType("factorName", "GEOGRAPHIC_RISK");
                    factor.stringType("factorDescription", "Standard-risk jurisdiction");
                    factor.numberType("weight", 0.1);
                }));
            });
            body.object("initialAccount", acct -> {
                acct.stringType("accountType", "CHECKING");
                acct.stringType("currency", "USD");
                acct.numberValue("initialDeposit", BigDecimal.valueOf(1000.00));
                acct.stringType("productCode", "CHK-STANDARD");
            });
        }).build();

        DslPart responseBody = LambdaDsl.newJsonBody((o) -> {
            o.booleanType("success", true);
            o.stringType("message", "Customer onboarded successfully");
            o.object("data", data -> {
                data.uuid("id");
                data.stringMatcher("customerNumber", "CUST-.*", "CUST-0001-20241101");
                data.stringType("email", "pact.customer@example.com");
                data.stringType("customerStatus", "PENDING_VERIFICATION");
            });
            o.stringMatcher("timestamp", "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}", "2026-01-01T10:30:00");
        }).build();

        return builder
                .given("customer onboarding request is valid")
                .uponReceiving("a request to onboard a customer")
                .path("/api/v1/customers/onboard")
                .method("POST")
                .headers("Content-Type", MediaType.APPLICATION_JSON_VALUE, "Accept", MediaType.APPLICATION_JSON_VALUE)
                .body(requestBody)
                .willRespondWith()
                .status(201)
                .headers(Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .body(responseBody)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "onboardingSuccess")
    void verifyPactInteraction(MockServer mockServer) {
        given()
                .baseUri(mockServer.getUrl())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                    {
                      "personalInfo": {
                        "firstName": "Jane",
                        "lastName": "Smith",
                        "dateOfBirth": "1992-04-18",
                        "nationality": "US",
                        "taxIdentificationNumber": "987-65-1234",
                        "email": "pact.customer@example.com",
                        "phoneNumber": "+15551234567"
                      },
                      "addresses": [{
                        "addressType": "HOME",
                        "street": "789 Elm Street",
                        "city": "Chicago",
                        "state": "IL",
                        "postalCode": "60601",
                        "country": "US",
                        "isPrimary": true
                      }],
                      "kycDocuments": [{
                        "documentType": "PASSPORT",
                        "documentNumber": "P12345678",
                        "issuingAuthority": "U.S. Department of State",
                        "issuingCountry": "US",
                        "issueDate": "2018-01-15",
                        "expiryDate": "2030-01-15"
                      }],
                      "riskProfile": {
                        "riskLevel": "LOW",
                        "riskScore": 20,
                        "assessedBy": "AUTO_SCREENING_v2.1",
                        "factors": [{
                          "factorName": "GEOGRAPHIC_RISK",
                          "factorDescription": "Standard-risk jurisdiction",
                          "weight": 0.1
                        }]
                      },
                      "initialAccount": {
                        "accountType": "CHECKING",
                        "currency": "USD",
                        "initialDeposit": 1000.00,
                        "productCode": "CHK-STANDARD"
                      }
                    }
                """)
                .when()
                .post("/api/v1/customers/onboard")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("message", equalTo("Customer onboarded successfully"));
    }
}

