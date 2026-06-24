package com.bank.onboarding.api.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CustomerOnboardingRequest(
        PersonalInfo personalInfo,
        List<Address> addresses,
        List<KycDocument> kycDocuments,
        RiskProfile riskProfile,
        InitialAccount initialAccount
) {
    public record PersonalInfo(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String nationality,
            String taxIdentificationNumber,
            String email,
            String phoneNumber
    ) {
    }

    public record Address(
            String addressType,
            String street,
            String city,
            String state,
            String postalCode,
            String country,
            Boolean isPrimary
    ) {
    }

    public record KycDocument(
            String documentType,
            String documentNumber,
            String issuingAuthority,
            String issuingCountry,
            LocalDate issueDate,
            LocalDate expiryDate
    ) {
    }

    public record RiskProfile(
            String riskLevel,
            Integer riskScore,
            String assessedBy,
            String assessmentNotes,
            List<RiskFactor> factors
    ) {
    }

    public record RiskFactor(
            String factorName,
            String factorDescription,
            BigDecimal weight
    ) {
    }

    public record InitialAccount(
            String accountType,
            String currency,
            BigDecimal initialDeposit,
            String productCode
    ) {
    }
}

