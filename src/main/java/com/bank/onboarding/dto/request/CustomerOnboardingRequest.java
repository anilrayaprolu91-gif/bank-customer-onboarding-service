package com.bank.onboarding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level request DTO for the full customer onboarding flow.
 * Contains a complex nested structure covering personal info,
 * addresses, KYC documents, risk assessment and initial account.
 */
@Schema(description = "Complete customer onboarding request containing all required banking compliance information")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOnboardingRequest {

    @Schema(description = "Personal identification details of the customer")
    @NotNull(message = "Personal information is required")
    @Valid
    private PersonalInfoRequest personalInfo;

    @Schema(description = "List of customer addresses (at least one primary address required)")
    @NotEmpty(message = "At least one address is required")
    @Size(max = 5, message = "A maximum of 5 addresses are allowed")
    @Valid
    @Builder.Default
    private List<AddressRequest> addresses = new ArrayList<>();

    @Schema(description = "KYC identity documents for AML/compliance verification")
    @NotEmpty(message = "At least one KYC document is required")
    @Size(max = 10, message = "A maximum of 10 KYC documents are allowed")
    @Valid
    @Builder.Default
    private List<KycDocumentRequest> kycDocuments = new ArrayList<>();

    @Schema(description = "Initial risk assessment performed during onboarding screening")
    @NotNull(message = "Risk profile is required")
    @Valid
    private RiskProfileRequest riskProfile;

    @Schema(description = "Optional initial account to open alongside the customer profile")
    @Valid
    private AccountCreationRequest initialAccount;
}

