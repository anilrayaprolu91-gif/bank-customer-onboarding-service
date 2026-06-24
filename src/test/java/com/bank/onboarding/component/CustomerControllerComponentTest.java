package com.bank.onboarding.component;

import com.bank.onboarding.domain.enums.CustomerStatus;
import com.bank.onboarding.dto.request.CustomerOnboardingRequest;
import com.bank.onboarding.dto.request.CustomerStatusUpdateRequest;
import com.bank.onboarding.util.TestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Customer Controller – Component Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerControllerComponentTest extends AbstractComponentTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;

    private static String onboardedCustomerId;

    // ── POST /api/v1/customers/onboard ────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("POST /onboard – should create customer and return 201")
    void shouldOnboardCustomer() throws Exception {
        CustomerOnboardingRequest request = TestFixtures.validOnboardingRequest().build();

        MvcResult result = mockMvc.perform(post("/api/v1/customers/onboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.customerNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value(request.getPersonalInfo().getEmail()))
                .andExpect(jsonPath("$.data.customerStatus").value("PENDING_VERIFICATION"))
                .andExpect(jsonPath("$.data.addresses", hasSize(1)))
                .andExpect(jsonPath("$.data.kycDocuments", hasSize(1)))
                .andExpect(jsonPath("$.data.riskProfile.riskLevel").value("LOW"))
                .andExpect(jsonPath("$.data.riskProfile.riskFactors", hasSize(1)))
                .andExpect(jsonPath("$.data.accounts", hasSize(1)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        onboardedCustomerId = objectMapper.readTree(body).at("/data/id").asText();
        assertThat(onboardedCustomerId).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("POST /onboard – should return 409 when email is duplicate")
    void shouldReturn409ForDuplicateEmail() throws Exception {
        // Use the same email as test Order(1)
        CustomerOnboardingRequest request = TestFixtures.validOnboardingRequest()
                .personalInfo(TestFixtures.validPersonalInfo()
                        .email(objectMapper.readTree(
                                mockMvc.perform(get("/api/v1/customers/" + onboardedCustomerId))
                                        .andReturn().getResponse().getContentAsString())
                                .at("/data/email").asText())
                        .build())
                .build();

        mockMvc.perform(post("/api/v1/customers/onboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("email")));
    }

    @Test
    @Order(3)
    @DisplayName("POST /onboard – should return 400 when required fields missing")
    void shouldReturn400WhenFieldsMissing() throws Exception {
        // Send an empty JSON body – all required fields missing
        mockMvc.perform(post("/api/v1/customers/onboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("POST /onboard – should return 422 when no primary address provided")
    void shouldReturn422WhenNoPrimaryAddress() throws Exception {
        CustomerOnboardingRequest request = TestFixtures.validOnboardingRequest()
                .addresses(java.util.List.of(
                        TestFixtures.validAddress().isPrimary(false).build()))
                .build();

        mockMvc.perform(post("/api/v1/customers/onboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    // ── GET /api/v1/customers/{id} ────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("GET /{id} – should return full customer profile")
    void shouldGetCustomerById() throws Exception {
        mockMvc.perform(get("/api/v1/customers/" + onboardedCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(onboardedCustomerId))
                .andExpect(jsonPath("$.data.addresses").isArray())
                .andExpect(jsonPath("$.data.kycDocuments").isArray())
                .andExpect(jsonPath("$.data.riskProfile").isMap());
    }

    @Test
    @Order(6)
    @DisplayName("GET /{id} – should return 404 for unknown customer")
    void shouldReturn404ForUnknownCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/customers/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/v1/customers ─────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("GET / – should return paginated customer list")
    void shouldListCustomers() throws Exception {
        mockMvc.perform(get("/api/v1/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    // ── PATCH /api/v1/customers/{id}/status ───────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("PATCH /{id}/status – should update customer to ACTIVE")
    void shouldUpdateCustomerStatus() throws Exception {
        CustomerStatusUpdateRequest request = CustomerStatusUpdateRequest.builder()
                .status(CustomerStatus.ACTIVE)
                .reason("All documents verified by compliance team")
                .build();

        mockMvc.perform(patch("/api/v1/customers/" + onboardedCustomerId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.statusReason").value("All documents verified by compliance team"));
    }

    // ── GET /api/v1/customers/{id}/onboarding-status ─────────────────────────

    @Test
    @Order(9)
    @DisplayName("GET /{id}/onboarding-status – should return status summary")
    void shouldGetOnboardingStatus() throws Exception {
        mockMvc.perform(get("/api/v1/customers/" + onboardedCustomerId + "/onboarding-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerId").value(onboardedCustomerId))
                .andExpect(jsonPath("$.data.totalDocumentsSubmitted").isNumber())
                .andExpect(jsonPath("$.data.eligibleForServices").isBoolean());
    }

    // ── GET /api/v1/customers/number/{customerNumber} ─────────────────────────

    @Test
    @Order(10)
    @DisplayName("GET /number/{customerNumber} – should return customer by number")
    void shouldGetCustomerByNumber() throws Exception {
        // Fetch customer number from previous test
        String customerNumber = objectMapper.readTree(
                mockMvc.perform(get("/api/v1/customers/" + onboardedCustomerId))
                        .andReturn().getResponse().getContentAsString())
                .at("/data/customerNumber").asText();

        mockMvc.perform(get("/api/v1/customers/number/" + customerNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerNumber").value(customerNumber));
    }
}

