package com.bank.onboarding.unit.mapper;

import com.bank.onboarding.domain.entity.*;
import com.bank.onboarding.domain.enums.*;
import com.bank.onboarding.dto.request.*;
import com.bank.onboarding.dto.response.*;
import com.bank.onboarding.mapper.CustomerMapper;
import com.bank.onboarding.mapper.CustomerMapperImpl;
import com.bank.onboarding.util.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CustomerMapper – Unit Tests")
class CustomerMapperTest {

    // Use the generated MapStruct implementation directly (no Spring context needed)
    private final CustomerMapper customerMapper = new CustomerMapperImpl();

    @Test
    @DisplayName("should map AddressRequest to Address entity correctly")
    void shouldMapAddressRequest() {
        AddressRequest request = TestFixtures.validAddress().build();

        Address result = customerMapper.toAddress(request);

        assertThat(result).isNotNull();
        assertThat(result.getAddressType()).isEqualTo(AddressType.HOME);
        assertThat(result.getStreet()).isEqualTo("789 Elm Street");
        assertThat(result.getCity()).isEqualTo("Chicago");
        assertThat(result.getState()).isEqualTo("IL");
        assertThat(result.getPostalCode()).isEqualTo("60601");
        assertThat(result.getCountry()).isEqualTo("US");
        assertThat(result.getIsPrimary()).isTrue();
        // Entity fields should not be set by mapper
        assertThat(result.getId()).isNull();
        assertThat(result.getCustomer()).isNull();
    }

    @Test
    @DisplayName("should map Address entity to AddressResponse correctly")
    void shouldMapAddressEntity() {
        Customer customer = TestFixtures.buildCustomer();
        Address  address  = TestFixtures.buildAddress(customer);

        AddressResponse result = customerMapper.toAddressResponse(address);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(address.getId());
        assertThat(result.getAddressType()).isEqualTo(AddressType.HOME);
        assertThat(result.getCity()).isEqualTo("Chicago");
        assertThat(result.getIsPrimary()).isTrue();
    }

    @Test
    @DisplayName("should map KycDocumentRequest to KycDocument with PENDING status")
    void shouldMapKycDocumentRequest() {
        KycDocumentRequest request = TestFixtures.validKycDocument().build();

        KycDocument result = customerMapper.toKycDocument(request);

        assertThat(result).isNotNull();
        assertThat(result.getDocumentType()).isEqualTo(DocumentType.PASSPORT);
        assertThat(result.getIssuingCountry()).isEqualTo("US");
        assertThat(result.getIssueDate()).isEqualTo(LocalDate.of(2018, 1, 15));
        assertThat(result.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(result.getVerifiedBy()).isNull();
        assertThat(result.getVerifiedAt()).isNull();
    }

    @Test
    @DisplayName("should map RiskProfileRequest to RiskProfile with current assessedAt")
    void shouldMapRiskProfileRequest() {
        RiskProfileRequest request = TestFixtures.validRiskProfile().build();

        RiskProfile result = customerMapper.toRiskProfile(request);

        assertThat(result).isNotNull();
        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(result.getRiskScore()).isEqualTo(20);
        assertThat(result.getAssessedBy()).isEqualTo("AUTO_SCREENING_v2.1");
        assertThat(result.getAssessedAt()).isNotNull();
        assertThat(result.getCustomer()).isNull();
    }

    @Test
    @DisplayName("should map Customer entity to CustomerResponse with fullName computed")
    void shouldMapCustomerToResponse() {
        Customer customer = TestFixtures.buildCustomer();

        CustomerResponse result = customerMapper.toResponse(customer);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(customer.getId());
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getFullName()).isEqualTo("Jane Smith");
        assertThat(result.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(result.getCustomerStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    @DisplayName("should map Customer to OnboardingStatusResponse with computed counts")
    void shouldMapToOnboardingStatus() {
        Customer customer = TestFixtures.buildCustomer();
        // The customer built by TestFixtures has 1 VERIFIED KYC doc and 0 accounts

        OnboardingStatusResponse result = customerMapper.toOnboardingStatusResponse(customer);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customer.getId());
        assertThat(result.getTotalDocumentsSubmitted()).isEqualTo(1);
        assertThat(result.getVerifiedDocuments()).isEqualTo(1);
        assertThat(result.getPendingDocuments()).isEqualTo(0);
        assertThat(result.getTotalAccounts()).isEqualTo(0);
        assertThat(result.getEligibleForServices()).isTrue(); // ACTIVE customer
    }
}

