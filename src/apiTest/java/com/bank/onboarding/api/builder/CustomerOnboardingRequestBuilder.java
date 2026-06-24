package com.bank.onboarding.api.builder;

import com.bank.onboarding.api.model.request.CustomerOnboardingRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class CustomerOnboardingRequestBuilder {

    private String firstName = "Jane";
    private String lastName = "Smith";
    private LocalDate dateOfBirth = LocalDate.of(1992, 4, 18);
    private String nationality = "US";
    private String taxIdentificationNumber = "987-65-4321";
    private String email = "jane.smith@example.com";
    private String phoneNumber = "+15551234567";
    private String documentNumber = "P12345678";
    private LocalDate issueDate = LocalDate.of(2018, 1, 15);
    private LocalDate expiryDate = LocalDate.now().plusYears(5);
    private BigDecimal initialDeposit = new BigDecimal("1000.00");

    private CustomerOnboardingRequestBuilder() {
    }

    public static CustomerOnboardingRequestBuilder aCustomer() {
        return new CustomerOnboardingRequestBuilder();
    }

    public CustomerOnboardingRequestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public CustomerOnboardingRequestBuilder withTaxIdentificationNumber(String taxIdentificationNumber) {
        this.taxIdentificationNumber = taxIdentificationNumber;
        return this;
    }

    public CustomerOnboardingRequestBuilder withDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    public CustomerOnboardingRequestBuilder withIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public CustomerOnboardingRequestBuilder withExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public CustomerOnboardingRequestBuilder withInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
        return this;
    }

    public CustomerOnboardingRequest build() {
        return new CustomerOnboardingRequest(
                new CustomerOnboardingRequest.PersonalInfo(firstName, lastName, dateOfBirth, nationality,
                        taxIdentificationNumber, email, phoneNumber),
                List.of(new CustomerOnboardingRequest.Address("HOME", "789 Elm Street", "Chicago", "IL", "60601", "US", true)),
                List.of(new CustomerOnboardingRequest.KycDocument("PASSPORT", documentNumber, "U.S. Department of State",
                        "US", issueDate, expiryDate)),
                new CustomerOnboardingRequest.RiskProfile("LOW", 20, "AUTO_SCREENING_v2.1", null,
                        List.of(new CustomerOnboardingRequest.RiskFactor("GEOGRAPHIC_RISK", "Standard-risk jurisdiction",
                                new BigDecimal("0.10")))),
                new CustomerOnboardingRequest.InitialAccount("CHECKING", "USD", initialDeposit, "CHK-STANDARD")
        );
    }
}

