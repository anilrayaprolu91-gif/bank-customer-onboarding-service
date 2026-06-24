package com.bank.onboarding.api.tests;

import com.bank.onboarding.api.factory.TestDataFactory;
import com.bank.onboarding.api.model.request.AccountCreationRequest;
import com.bank.onboarding.api.model.request.CustomerOnboardingRequest;
import com.bank.onboarding.api.model.response.AccountResponse;
import com.bank.onboarding.api.model.response.CustomerResponse;
import com.bank.onboarding.api.support.JsonSupport;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Banking APIs")
@Feature("Account lifecycle")
class AccountLifecycleApiTest extends BaseApiTest {

    @Test
    @Description("Creates a savings account for an activated customer and verifies retrieval and closure.")
    void shouldCreateRetrieveAndCloseSavingsAccount() {
        CustomerOnboardingRequest onboardingRequest = TestDataFactory.newCustomerOnboardingRequest();
        Response onboardResponse = customerApiClient.onboardCustomer(onboardingRequest);
        onboardResponse.then().statusCode(201);

        CustomerResponse customer = JsonSupport.readData(onboardResponse, CustomerResponse.class);

        Response updateStatusResponse = customerApiClient.updateCustomerStatus(customer.id().toString(), TestDataFactory.activeCustomerStatus());
        updateStatusResponse.then().statusCode(200);

        AccountCreationRequest accountRequest = TestDataFactory.newSavingsAccountRequest();
        Response createAccountResponse = accountApiClient.createAccount(customer.id().toString(), accountRequest);

        createAccountResponse.then()
                .statusCode(201)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/account-response-schema.json"));

        AccountResponse account = JsonSupport.readData(createAccountResponse, AccountResponse.class);
        assertThat(account.customerId()).isEqualTo(customer.id());
        assertThat(account.accountType()).isEqualTo("SAVINGS");
        assertThat(account.accountStatus()).isEqualTo("ACTIVE");

        Response getAccountResponse = accountApiClient.getAccountById(account.id().toString());
        getAccountResponse.then().statusCode(200);
        AccountResponse fetchedAccount = JsonSupport.readData(getAccountResponse, AccountResponse.class);
        assertThat(fetchedAccount.accountNumber()).isEqualTo(account.accountNumber());

        Response getAccountsResponse = accountApiClient.getAccountsByCustomerId(customer.id().toString());
        getAccountsResponse.then().statusCode(200);
        List<UUID> accountIds = JsonSupport.readTree(getAccountsResponse)
                .get("data")
                .findValuesAsText("id")
                .stream()
                .map(UUID::fromString)
                .toList();
        assertThat(accountIds).contains(account.id());

        Response closeAccountResponse = accountApiClient.closeAccount(account.id().toString());
        closeAccountResponse.then().statusCode(200);
        AccountResponse closedAccount = JsonSupport.readData(closeAccountResponse, AccountResponse.class);
        assertThat(closedAccount.accountStatus()).isEqualTo("CLOSED");
        assertThat(closedAccount.closedDate()).isNotNull();
    }
}

