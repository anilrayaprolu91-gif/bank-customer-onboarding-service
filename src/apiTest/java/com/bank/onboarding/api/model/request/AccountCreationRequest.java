package com.bank.onboarding.api.model.request;

import java.math.BigDecimal;

public record AccountCreationRequest(
        String accountType,
        String currency,
        BigDecimal initialDeposit,
        String productCode
) {
}

