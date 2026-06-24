package com.bank.onboarding.api.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        UUID customerId,
        String accountType,
        String currency,
        BigDecimal balance,
        String accountStatus,
        LocalDateTime openedDate,
        LocalDateTime closedDate,
        String productCode,
        BigDecimal interestRate
) {
}

