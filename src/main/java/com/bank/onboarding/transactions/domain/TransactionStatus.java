package com.bank.onboarding.transactions.domain;

/**
 * Transaction status enum.
 */
public enum TransactionStatus {
    PENDING,
    CONFIRMED,
    SETTLED,
    REVERSED,
    FAILED,
    CANCELLED
}

