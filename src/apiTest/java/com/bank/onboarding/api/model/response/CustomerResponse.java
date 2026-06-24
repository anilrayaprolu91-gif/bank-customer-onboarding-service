package com.bank.onboarding.api.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String customerNumber,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        String nationality,
        String taxIdentificationNumber,
        String customerStatus,
        String statusReason,
        LocalDateTime onboardingDate,
        List<AddressResponse> addresses,
        List<KycDocumentResponse> kycDocuments,
        RiskProfileResponse riskProfile,
        List<AccountSummary> accounts
) {
    public record AddressResponse(
            UUID id,
            String addressType,
            String street,
            String city,
            String state,
            String postalCode,
            String country,
            Boolean isPrimary
    ) {
    }

    public record KycDocumentResponse(
            UUID id,
            String documentType,
            String documentNumber,
            String issuingAuthority,
            String issuingCountry,
            LocalDate issueDate,
            LocalDate expiryDate,
            String verificationStatus
    ) {
    }

    public record RiskProfileResponse(
            UUID id,
            String riskLevel,
            Integer riskScore,
            String assessedBy,
            LocalDateTime assessedAt,
            List<RiskFactorResponse> riskFactors
    ) {
    }

    public record RiskFactorResponse(
            UUID id,
            String factorName,
            String factorDescription,
            BigDecimal weight
    ) {
    }

    public record AccountSummary(
            UUID id,
            String accountNumber,
            UUID customerId,
            String accountType,
            String currency,
            BigDecimal balance,
            String accountStatus
    ) {
    }
}

