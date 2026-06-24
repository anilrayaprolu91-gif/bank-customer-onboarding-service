package com.bank.onboarding.dto.response;

import com.bank.onboarding.domain.enums.CustomerStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Summary of a customer's current onboarding status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnboardingStatusResponse {

    @Schema(description = "Customer identifier")
    private UUID customerId;

    @Schema(description = "Customer number", example = "CUST-0001-20241101")
    private String customerNumber;

    @Schema(description = "Current onboarding status", example = "ACTIVE")
    private CustomerStatus customerStatus;

    @Schema(description = "Reason for the current status")
    private String statusReason;

    @Schema(description = "Total KYC documents submitted")
    private Integer totalDocumentsSubmitted;

    @Schema(description = "Number of verified KYC documents")
    private Integer verifiedDocuments;

    @Schema(description = "Number of pending KYC documents")
    private Integer pendingDocuments;

    @Schema(description = "Number of linked accounts")
    private Integer totalAccounts;

    @Schema(description = "Date/time the status was last changed")
    private LocalDateTime statusChangedAt;

    @Schema(description = "Whether the customer is eligible for full banking services")
    private Boolean eligibleForServices;
}

