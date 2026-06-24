package com.bank.onboarding.api.tests;

import com.bank.onboarding.api.client.AccountApiClient;
import com.bank.onboarding.api.client.CustomerApiClient;
import com.bank.onboarding.api.config.ApiProfile;
import com.bank.onboarding.api.config.EnvironmentConfig;
import com.bank.onboarding.api.config.EnvironmentConfigResolver;
import com.bank.onboarding.api.factory.ApiClientFactory;
import com.bank.onboarding.api.support.ContainerizedApplicationManager;
import com.bank.onboarding.api.support.EmbeddedApplicationManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseApiTest {

    protected EnvironmentConfig environmentConfig;
    protected CustomerApiClient customerApiClient;
    protected AccountApiClient accountApiClient;

    @BeforeAll
    void bootstrapSuite() {
        ApiProfile profile = ApiProfile.from(System.getProperty("api.profile", "embedded"));
        environmentConfig = EnvironmentConfigResolver.resolve(profile);

        if (profile == ApiProfile.CONTAINER) {
            String baseUrl = ContainerizedApplicationManager.start(environmentConfig);
            environmentConfig = environmentConfig.withBaseUrl(baseUrl);
        } else if (profile == ApiProfile.EMBEDDED) {
            environmentConfig = environmentConfig.withBaseUrl(EmbeddedApplicationManager.start());
        }

        customerApiClient = ApiClientFactory.customerClient(environmentConfig.baseUrl());
        accountApiClient = ApiClientFactory.accountClient(environmentConfig.baseUrl());
    }
}

