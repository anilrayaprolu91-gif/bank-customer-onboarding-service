package com.bank.onboarding.api.builder;

import com.bank.onboarding.api.model.request.AccountCreationRequest;

import java.math.BigDecimal;

public final class AccountCreationRequestBuilder {

    private String accountType = "SAVINGS";
    private String currency = "AUD";
    private BigDecimal initialDeposit = new BigDecimal("5000.00");
    private String productCode = "SAV-STANDARD";

    private AccountCreationRequestBuilder() {
    }

    public static AccountCreationRequestBuilder anAccount() {
        return new AccountCreationRequestBuilder();
    }

    public AccountCreationRequestBuilder withAccountType(String accountType) {
        this.accountType = accountType;
        return this;
    }

    public AccountCreationRequestBuilder withCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public AccountCreationRequestBuilder withInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
        return this;
    }

    public AccountCreationRequestBuilder withProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public AccountCreationRequest build() {
        return new AccountCreationRequest(accountType, currency, initialDeposit, productCode);
    }
}

