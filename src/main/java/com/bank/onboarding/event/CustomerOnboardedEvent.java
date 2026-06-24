package com.bank.onboarding.event;

import java.time.Instant;

public record CustomerOnboardedEvent(
        String customerId,
        String customerNumber,
        String email,
        Instant occurredAt
) {
}

