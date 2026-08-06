package com.bank.onboarding.onboarding.application;

import com.bank.onboarding.dto.request.CustomerOnboardingRequest;
import com.bank.onboarding.dto.response.CustomerResponse;

public interface CustomerOnboardingService {

    CustomerResponse onboardCustomer(CustomerOnboardingRequest request);
}

