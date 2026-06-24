package com.bank.onboarding.api.model.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record OnboardingStatusResponse(
        UUID customerId,
        String customerNumber,
        String customerStatus,
        String statusReason,
        Integer totalDocumentsSubmitted,
        Integer verifiedDocuments,
        Integer pendingDocuments,
        Integer totalAccounts,
        LocalDateTime statusChangedAt,
        Boolean eligibleForServices
) {
}

