package com.bank.onboarding.component;

import com.bank.onboarding.domain.enums.AccountType;
import com.bank.onboarding.domain.enums.CustomerStatus;
import com.bank.onboarding.dto.request.AccountCreationRequest;
import com.bank.onboarding.dto.request.CustomerOnboardingRequest;
import com.bank.onboarding.dto.request.CustomerStatusUpdateRequest;
import com.bank.onboarding.util.TestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Account Controller – Component Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerComponentTest extends AbstractComponentTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String activeCustomerId;
    private String createdAccountId;

    // ── Setup: create and activate a customer ─────────────────────────────────

    @BeforeAll
    void onboardAndActivateCustomer() throws Exception {

        // 1. Onboard without initial account
        CustomerOnboardingRequest request = TestFixtures.validOnboardingRequest()
                .initialAccount(null)
                .build();

        MvcResult onboardResult = mockMvc.perform(post("/api/v1/customers/onboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        activeCustomerId = objectMapper.readTree(
                onboardResult.getResponse().getContentAsString()).at("/data/id").asText();

        // 2. Activate the customer
        CustomerStatusUpdateRequest activate = CustomerStatusUpdateRequest.builder()
                .status(CustomerStatus.ACTIVE)
                .reason("Setup for account tests")
                .build();

        mockMvc.perform(patch("/api/v1/customers/" + activeCustomerId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activate)))
                .andExpect(status().isOk());
    }

    // ── POST /api/v1/customers/{customerId}/accounts ──────────────────────────

    @Test
    @Order(1)
    @DisplayName("POST /customers/{id}/accounts – should create CHECKING account")
    void shouldCreateCheckingAccount() throws Exception {
        AccountCreationRequest request = AccountCreationRequest.builder()
                .accountType(AccountType.CHECKING)
                .currency("USD")
                .initialDeposit(BigDecimal.valueOf(2500.00))
                .productCode("CHK-STANDARD")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/customers/" + activeCustomerId + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.accountType").value("CHECKING"))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.balance").value(2500.00))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"))
                .andReturn();

        createdAccountId = objectMapper.readTree(
                result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    @Test
    @Order(2)
    @DisplayName("POST /customers/{id}/accounts – should create SAVINGS account")
    void shouldCreateSavingsAccount() throws Exception {
        AccountCreationRequest request = AccountCreationRequest.builder()
                .accountType(AccountType.SAVINGS)
                .currency("USD")
                .initialDeposit(BigDecimal.valueOf(10000.00))
                .build();

        mockMvc.perform(post("/api/v1/customers/" + activeCustomerId + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.data.balance").value(10000.00));
    }

    @Test
    @Order(3)
    @DisplayName("POST /customers/{id}/accounts – should return 404 for unknown customer")
    void shouldReturn404ForUnknownCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/customers/" + UUID.randomUUID() + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                TestFixtures.validAccountCreation().build())))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    @DisplayName("POST /customers/{id}/accounts – should return 400 for invalid request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        // Missing required fields
        mockMvc.perform(post("/api/v1/customers/" + activeCustomerId + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/v1/customers/{customerId}/accounts ───────────────────────────

    @Test
    @Order(5)
    @DisplayName("GET /customers/{id}/accounts – should list all accounts")
    void shouldListAccountsForCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/customers/" + activeCustomerId + "/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    // ── GET /api/v1/accounts/{accountId} ─────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("GET /accounts/{id} – should return account by ID")
    void shouldGetAccountById() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + createdAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(createdAccountId))
                .andExpect(jsonPath("$.data.customerId").value(activeCustomerId));
    }

    @Test
    @Order(7)
    @DisplayName("GET /accounts/{id} – should return 404 for unknown account")
    void shouldReturn404ForUnknownAccount() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/v1/accounts/{accountId} ──────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("DELETE /accounts/{id} – should close an active account")
    void shouldCloseAccount() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/" + createdAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus").value("CLOSED"))
                .andExpect(jsonPath("$.data.closedDate").isNotEmpty());
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /accounts/{id} – should return 422 when already closed")
    void shouldReturn422WhenAlreadyClosed() throws Exception {
        // Account was closed in previous test
        mockMvc.perform(delete("/api/v1/accounts/" + createdAccountId))
                .andExpect(status().isUnprocessableEntity());
    }
}

