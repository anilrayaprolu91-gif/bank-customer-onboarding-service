package com.bank.onboarding.service;

import com.bank.onboarding.dto.request.AccountCreationRequest;
import com.bank.onboarding.dto.response.AccountResponse;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    /**
     * Opens a new account for an existing customer.
     */
    AccountResponse createAccount(UUID customerId, AccountCreationRequest request);

    /**
     * Retrieves a single account by its UUID.
     */
    AccountResponse getAccountById(UUID accountId);

    /**
     * Retrieves all accounts for a given customer.
     */
    List<AccountResponse> getAccountsByCustomerId(UUID customerId);

    /**
     * Closes an account (sets status to CLOSED and records closing date).
     */
    AccountResponse closeAccount(UUID accountId);
}

