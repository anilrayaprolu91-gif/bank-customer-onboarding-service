package com.bank.onboarding.mapper;

import com.bank.onboarding.customer.domain.Address;
import com.bank.onboarding.customer.domain.Customer;
import com.bank.onboarding.kyc.domain.KycDocument;
import com.bank.onboarding.risk.domain.RiskFactor;
import com.bank.onboarding.risk.domain.RiskProfile;
import com.bank.onboarding.dto.request.*;
import com.bank.onboarding.dto.response.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Customer and all its nested sub-entities.
 * Spring component model is configured via the Gradle compiler arg
 * {@code -Amapstruct.defaultComponentModel=spring}.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CustomerMapper {

    // â”€â”€ Customer â†’ CustomerResponse â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Mapping(target = "fullName", expression = "java(customer.getFirstName() + \" \" + customer.getLastName())")
    CustomerResponse toResponse(Customer customer);

    List<CustomerResponse> toResponseList(List<Customer> customers);

    // â”€â”€ Address â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Mapping(target = "id",       ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address toAddress(AddressRequest request);

    AddressResponse toAddressResponse(Address address);

    // â”€â”€ KycDocument â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Mapping(target = "id",                 ignore = true)
    @Mapping(target = "customer",           ignore = true)
    @Mapping(target = "verificationStatus", constant = "PENDING")
    @Mapping(target = "verifiedBy",         ignore = true)
    @Mapping(target = "verifiedAt",         ignore = true)
    @Mapping(target = "rejectionReason",    ignore = true)
    @Mapping(target = "createdAt",          ignore = true)
    @Mapping(target = "updatedAt",          ignore = true)
    KycDocument toKycDocument(KycDocumentRequest request);

    KycDocumentResponse toKycDocumentResponse(KycDocument document);

    // â”€â”€ RiskProfile â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "customer",     ignore = true)
    @Mapping(target = "assessedAt",   expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "nextReviewDate", ignore = true)
    @Mapping(target = "riskFactors",  ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    RiskProfile toRiskProfile(RiskProfileRequest request);

    RiskProfileResponse toRiskProfileResponse(RiskProfile riskProfile);

    // â”€â”€ RiskFactor â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "riskProfile", ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    RiskFactor toRiskFactor(RiskFactorRequest request);

    RiskFactorResponse toRiskFactorResponse(RiskFactor riskFactor);

    // â”€â”€ OnboardingStatusResponse â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Mapping(target = "customerId",            source = "id")
    @Mapping(target = "totalDocumentsSubmitted", expression = "java(customer.getKycDocuments().size())")
    @Mapping(target = "verifiedDocuments",     expression = "java((int) customer.getKycDocuments().stream().filter(d -> d.getVerificationStatus() == com.bank.onboarding.kyc.domain.VerificationStatus.VERIFIED).count())")
    @Mapping(target = "pendingDocuments",      expression = "java((int) customer.getKycDocuments().stream().filter(d -> d.getVerificationStatus() == com.bank.onboarding.kyc.domain.VerificationStatus.PENDING).count())")
    @Mapping(target = "totalAccounts",         expression = "java(customer.getAccounts().size())")
    @Mapping(target = "statusChangedAt",       source = "updatedAt")
    @Mapping(target = "eligibleForServices",   expression = "java(customer.getCustomerStatus() == com.bank.onboarding.customer.domain.CustomerStatus.ACTIVE)")
    OnboardingStatusResponse toOnboardingStatusResponse(Customer customer);
}


