package com.bank.onboarding.api.tests;

import com.bank.onboarding.api.factory.TestDataFactory;
import com.bank.onboarding.api.model.request.CustomerOnboardingRequest;
import com.bank.onboarding.api.model.request.CustomerStatusUpdateRequest;
import com.bank.onboarding.api.model.response.CustomerResponse;
import com.bank.onboarding.api.model.response.OnboardingStatusResponse;
import com.bank.onboarding.api.support.JsonSupport;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Banking APIs")
@Feature("Customer onboarding")
class CustomerOnboardingApiTest extends BaseApiTest {

    @Test
    @Owner("qa-platform")
    @Description("Onboards a new customer, validates schemas, and verifies the activation lifecycle.")
    void shouldOnboardAndActivateCustomer() {
        CustomerOnboardingRequest onboardingRequest = TestDataFactory.newCustomerOnboardingRequest();

        Response onboardResponse = customerApiClient.onboardCustomer(onboardingRequest);

        onboardResponse.then()
                .statusCode(201)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/customer-onboard-response-schema.json"));

        CustomerResponse customer = JsonSupport.readData(onboardResponse, CustomerResponse.class);
        assertThat(customer.id()).isNotNull();
        assertThat(customer.email()).isEqualTo(onboardingRequest.personalInfo().email());
        assertThat(customer.customerStatus()).isEqualTo("PENDING_VERIFICATION");
        assertThat(customer.addresses()).hasSize(1);
        assertThat(customer.kycDocuments()).hasSize(1);
        assertThat(customer.riskProfile()).isNotNull();
        assertThat(customer.accounts()).hasSize(1);

        Response getCustomerResponse = customerApiClient.getCustomerById(customer.id().toString());
        getCustomerResponse.then().statusCode(200);

        CustomerResponse fetchedCustomer = JsonSupport.readData(getCustomerResponse, CustomerResponse.class);
        assertThat(fetchedCustomer.customerNumber()).isEqualTo(customer.customerNumber());
        assertThat(fetchedCustomer.fullName()).isEqualTo("Jane Smith");

        Response getByNumberResponse = customerApiClient.getCustomerByNumber(customer.customerNumber());
        getByNumberResponse.then().statusCode(200);
        CustomerResponse customerByNumber = JsonSupport.readData(getByNumberResponse, CustomerResponse.class);
        assertThat(customerByNumber.id()).isEqualTo(customer.id());

        CustomerStatusUpdateRequest statusUpdateRequest = TestDataFactory.activeCustomerStatus();
        Response updateStatusResponse = customerApiClient.updateCustomerStatus(customer.id().toString(), statusUpdateRequest);
        updateStatusResponse.then().statusCode(200);

        CustomerResponse activeCustomer = JsonSupport.readData(updateStatusResponse, CustomerResponse.class);
        assertThat(activeCustomer.customerStatus()).isEqualTo("ACTIVE");
        assertThat(activeCustomer.statusReason()).isEqualTo(statusUpdateRequest.reason());

        Response onboardingStatusResponse = customerApiClient.getOnboardingStatus(customer.id().toString());
        onboardingStatusResponse.then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/onboarding-status-response-schema.json"));

        OnboardingStatusResponse onboardingStatus = JsonSupport.readData(onboardingStatusResponse, OnboardingStatusResponse.class);
        assertThat(onboardingStatus.customerId()).isEqualTo(customer.id());
        assertThat(onboardingStatus.customerStatus()).isEqualTo("ACTIVE");
        assertThat(onboardingStatus.eligibleForServices()).isTrue();
        assertThat(onboardingStatus.totalDocumentsSubmitted()).isGreaterThanOrEqualTo(1);
    }
}

