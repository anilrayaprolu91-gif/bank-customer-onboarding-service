package com.bank.onboarding.util;

import com.bank.onboarding.domain.entity.*;
import com.bank.onboarding.domain.enums.*;
import com.bank.onboarding.dto.request.*;
import com.bank.onboarding.dto.response.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Central factory for building test fixtures.
 * All builders are fluent so tests can override only the fields they care about.
 */
public final class TestFixtures {

    private TestFixtures() {}

    // ── Request builders ──────────────────────────────────────────────────────

    public static CustomerOnboardingRequest.CustomerOnboardingRequestBuilder validOnboardingRequest() {
        return CustomerOnboardingRequest.builder()
                .personalInfo(validPersonalInfo().build())
                .addresses(List.of(validAddress().build()))
                .kycDocuments(List.of(validKycDocument().build()))
                .riskProfile(validRiskProfile().build())
                .initialAccount(validAccountCreation().build());
    }

    public static PersonalInfoRequest.PersonalInfoRequestBuilder validPersonalInfo() {
        return PersonalInfoRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1992, 4, 18))
                .nationality("US")
                .taxIdentificationNumber("987-65-" + UUID.randomUUID().toString().substring(0, 4))
                .email("jane.smith." + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                .phoneNumber("+15551234567");
    }

    public static AddressRequest.AddressRequestBuilder validAddress() {
        return AddressRequest.builder()
                .addressType(AddressType.HOME)
                .street("789 Elm Street")
                .city("Chicago")
                .state("IL")
                .postalCode("60601")
                .country("US")
                .isPrimary(true);
    }

    public static KycDocumentRequest.KycDocumentRequestBuilder validKycDocument() {
        return KycDocumentRequest.builder()
                .documentType(DocumentType.PASSPORT)
                .documentNumber("P" + System.currentTimeMillis())
                .issuingAuthority("U.S. Department of State")
                .issuingCountry("US")
                .issueDate(LocalDate.of(2018, 1, 15))
                .expiryDate(LocalDate.now().plusYears(5));
    }

    public static RiskProfileRequest.RiskProfileRequestBuilder validRiskProfile() {
        return RiskProfileRequest.builder()
                .riskLevel(RiskLevel.LOW)
                .riskScore(20)
                .assessedBy("AUTO_SCREENING_v2.1")
                .factors(List.of(validRiskFactor().build()));
    }

    public static RiskFactorRequest.RiskFactorRequestBuilder validRiskFactor() {
        return RiskFactorRequest.builder()
                .factorName("GEOGRAPHIC_RISK")
                .factorDescription("Standard-risk jurisdiction")
                .weight(BigDecimal.valueOf(0.10));
    }

    public static AccountCreationRequest.AccountCreationRequestBuilder validAccountCreation() {
        return AccountCreationRequest.builder()
                .accountType(AccountType.CHECKING)
                .currency("USD")
                .initialDeposit(BigDecimal.valueOf(1000.00))
                .productCode("CHK-STANDARD");
    }

    // ── Entity builders ───────────────────────────────────────────────────────

    public static Customer buildCustomer() {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .customerNumber("CUST-0001-20241101")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("+15551234567")
                .dateOfBirth(LocalDate.of(1992, 4, 18))
                .nationality("US")
                .taxIdentificationNumber("987-65-4321")
                .customerStatus(CustomerStatus.ACTIVE)
                .onboardingDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Address address = buildAddress(customer);
        customer.getAddresses().add(address);

        KycDocument doc = buildKycDocument(customer);
        customer.getKycDocuments().add(doc);

        RiskProfile rp = buildRiskProfile(customer);
        customer.setRiskProfile(rp);

        return customer;
    }

    public static Address buildAddress(Customer customer) {
        return Address.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .addressType(AddressType.HOME)
                .street("789 Elm Street")
                .city("Chicago")
                .state("IL")
                .postalCode("60601")
                .country("US")
                .isPrimary(true)
                .build();
    }

    public static KycDocument buildKycDocument(Customer customer) {
        return KycDocument.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .documentType(DocumentType.PASSPORT)
                .documentNumber("P9876543210")
                .issuingAuthority("U.S. Department of State")
                .issuingCountry("US")
                .issueDate(LocalDate.of(2018, 1, 15))
                .expiryDate(LocalDate.now().plusYears(5))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
    }

    public static RiskProfile buildRiskProfile(Customer customer) {
        RiskProfile rp = RiskProfile.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .riskLevel(RiskLevel.LOW)
                .riskScore(20)
                .assessedAt(LocalDateTime.now())
                .assessedBy("AUTO_SCREENING_v2.1")
                .nextReviewDate(LocalDate.now().plusYears(1))
                .build();

        RiskFactor rf = RiskFactor.builder()
                .id(UUID.randomUUID())
                .riskProfile(rp)
                .factorName("GEOGRAPHIC_RISK")
                .factorDescription("Standard-risk jurisdiction")
                .weight(BigDecimal.valueOf(0.10))
                .build();
        rp.getRiskFactors().add(rf);
        return rp;
    }

    public static Account buildAccount(Customer customer) {
        return Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("ACC-0001-1234567")
                .customer(customer)
                .accountType(AccountType.CHECKING)
                .currency("USD")
                .balance(BigDecimal.valueOf(1000.00))
                .accountStatus(AccountStatus.ACTIVE)
                .openedDate(LocalDateTime.now())
                .productCode("CHK-STANDARD")
                .build();
    }

    // ── Response builders ─────────────────────────────────────────────────────

    public static CustomerResponse buildCustomerResponse(UUID id) {
        return CustomerResponse.builder()
                .id(id)
                .customerNumber("CUST-0001-20241101")
                .firstName("Jane")
                .lastName("Smith")
                .fullName("Jane Smith")
                .email("jane.smith@example.com")
                .customerStatus(CustomerStatus.ACTIVE)
                .onboardingDate(LocalDateTime.now())
                .build();
    }

    public static AccountResponse buildAccountResponse(UUID accountId, UUID customerId) {
        return AccountResponse.builder()
                .id(accountId)
                .accountNumber("ACC-0001-1234567")
                .customerId(customerId)
                .accountType(AccountType.CHECKING)
                .currency("USD")
                .balance(BigDecimal.valueOf(1000.00))
                .accountStatus(AccountStatus.ACTIVE)
                .openedDate(LocalDateTime.now())
                .build();
    }
}

