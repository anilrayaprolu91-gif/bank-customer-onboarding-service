package com.bank.onboarding.dto.response;

import com.bank.onboarding.domain.enums.CustomerStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full customer response DTO with all nested sub-resources.
 * This is the primary response type for customer onboarding operations.
 */
@Schema(description = "Full customer profile including all related banking information")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResponse {

    @Schema(description = "Unique customer identifier (UUID)")
    private UUID id;

    @Schema(description = "System-generated customer number", example = "CUST-0001-20241101")
    private String customerNumber;

    @Schema(description = "Legal first name", example = "John")
    private String firstName;

    @Schema(description = "Legal last name", example = "Doe")
    private String lastName;

    @Schema(description = "Full name (computed)", example = "John Doe")
    private String fullName;

    @Schema(description = "Primary email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Phone number in E.164 format", example = "+15550123456")
    private String phoneNumber;

    @Schema(description = "Date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;

    @Schema(description = "ISO nationality code", example = "US")
    private String nationality;

    @Schema(description = "Tax identification number", example = "123-45-6789")
    private String taxIdentificationNumber;

    @Schema(description = "Current onboarding/account status", example = "ACTIVE")
    private CustomerStatus customerStatus;

    @Schema(description = "Reason for the current status")
    private String statusReason;

    @Schema(description = "Date and time the customer was first onboarded")
    private LocalDateTime onboardingDate;

    @Schema(description = "List of customer addresses")
    private List<AddressResponse> addresses;

    @Schema(description = "List of KYC compliance documents")
    private List<KycDocumentResponse> kycDocuments;

    @Schema(description = "Risk profile assessed during onboarding")
    private RiskProfileResponse riskProfile;

    @Schema(description = "Bank accounts linked to this customer")
    private List<AccountResponse> accounts;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last-updated timestamp")
    private LocalDateTime updatedAt;
}

