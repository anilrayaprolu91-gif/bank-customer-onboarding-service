package com.bank.onboarding.api.client;

import com.bank.onboarding.api.model.request.AccountCreationRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class AccountApiClient {

    private final RequestSpecification specification;

    public AccountApiClient(RequestSpecification specification) {
        this.specification = specification;
    }

    public Response createAccount(String customerId, AccountCreationRequest request) {
        return RestAssured.given()
                .spec(specification)
                .pathParam("customerId", customerId)
                .body(request)
                .when()
                .post("/api/v1/customers/{customerId}/accounts")
                .andReturn();
    }

    public Response getAccountsByCustomerId(String customerId) {
        return RestAssured.given()
                .spec(specification)
                .pathParam("customerId", customerId)
                .when()
                .get("/api/v1/customers/{customerId}/accounts")
                .andReturn();
    }

    public Response getAccountById(String accountId) {
        return RestAssured.given()
                .spec(specification)
                .pathParam("accountId", accountId)
                .when()
                .get("/api/v1/accounts/{accountId}")
                .andReturn();
    }

    public Response closeAccount(String accountId) {
        return RestAssured.given()
                .spec(specification)
                .pathParam("accountId", accountId)
                .when()
                .delete("/api/v1/accounts/{accountId}")
                .andReturn();
    }
}

