package com.bank.onboarding.api.model.request;

public record CustomerStatusUpdateRequest(
        String status,
        String reason
) {
}

