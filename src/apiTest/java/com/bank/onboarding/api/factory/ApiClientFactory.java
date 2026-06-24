package com.bank.onboarding.api.factory;

import com.bank.onboarding.api.client.AccountApiClient;
import com.bank.onboarding.api.client.CustomerApiClient;
import io.restassured.specification.RequestSpecification;

public final class ApiClientFactory {

    private ApiClientFactory() {
    }

    public static CustomerApiClient customerClient(String baseUrl) {
        RequestSpecification specification = RequestSpecificationFactory.create(baseUrl);
        return new CustomerApiClient(specification);
    }

    public static AccountApiClient accountClient(String baseUrl) {
        RequestSpecification specification = RequestSpecificationFactory.create(baseUrl);
        return new AccountApiClient(specification);
    }
}

