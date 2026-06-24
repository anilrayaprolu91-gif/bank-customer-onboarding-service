package com.bank.onboarding.api.client;

import com.bank.onboarding.api.model.request.CustomerOnboardingRequest;
import com.bank.onboarding.api.model.request.CustomerStatusUpdateRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class CustomerApiClient {

    private final RequestSpecification specification;

    public CustomerApiClient(RequestSpecification specification) {
        this.specification = specification;
    }

    public Response onboardCustomer(CustomerOnboardingRequest request) {
        return RestAssured.given()
                .spec(specification)
                .body(request)
                .when()
                .post("/api/v1/customers/onboard")
                .andReturn();
    }

    public Response getCustomerById(String customerId) {
        return RestAssured.given()
                .spec(specification)
                .pathParam("customerId", customerId)
                .when()
                .get("/api/v1/customers/{customerId}")
                .andReturn();
    }

    public Response getCustomerByNumber(String customerNumber) {
        return RestAssured.given()
                .spec(specification)
                .pathParam("customerNumber", customerNumber)
                .when()
                .get("/api/v1/customers/number/{customerNumber}")
                .andReturn();
    }

    public Response updateCustomerStatus(String customerId, CustomerStatusUpdateRequest request) {
        return RestAssured.given()
                .spec(specification)
                .pathParam("customerId", customerId)
                .body(request)
                .when()
                .patch("/api/v1/customers/{customerId}/status")
                .andReturn();
    }

    public Response getOnboardingStatus(String customerId) {
        return RestAssured.given()
                .spec(specification)
                .pathParam("customerId", customerId)
                .when()
                .get("/api/v1/customers/{customerId}/onboarding-status")
                .andReturn();
    }
}

