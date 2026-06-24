package com.bank.onboarding.api.factory;

import com.bank.onboarding.api.builder.AccountCreationRequestBuilder;
import com.bank.onboarding.api.builder.CustomerOnboardingRequestBuilder;
import com.bank.onboarding.api.builder.CustomerStatusUpdateRequestBuilder;
import com.bank.onboarding.api.model.request.AccountCreationRequest;
import com.bank.onboarding.api.model.request.CustomerOnboardingRequest;
import com.bank.onboarding.api.model.request.CustomerStatusUpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static CustomerOnboardingRequest newCustomerOnboardingRequest() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        return CustomerOnboardingRequestBuilder.aCustomer()
                .withEmail("api.customer." + uniqueSuffix + "@example.com")
                .withTaxIdentificationNumber("TAX-" + uniqueSuffix)
                .withDocumentNumber("P" + uniqueSuffix.toUpperCase())
                .withIssueDate(LocalDate.of(2018, 1, 15))
                .withExpiryDate(LocalDate.now().plusYears(4))
                .withInitialDeposit(new BigDecimal("1500.00"))
                .build();
    }

    public static AccountCreationRequest newSavingsAccountRequest() {
        return AccountCreationRequestBuilder.anAccount()
                .withAccountType("SAVINGS")
                .withCurrency("USD")
                .withInitialDeposit(new BigDecimal("5000.00"))
                .withProductCode("SAV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .build();
    }

    public static CustomerStatusUpdateRequest activeCustomerStatus() {
        return CustomerStatusUpdateRequestBuilder.aStatusUpdate()
                .withStatus("ACTIVE")
                .withReason("Customer cleared all KYC and AML checks")
                .build();
    }
}

