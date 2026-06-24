package com.bank.onboarding.exception;

public class DuplicateCustomerException extends RuntimeException {

    public DuplicateCustomerException(String field, String value) {
        super("A customer with " + field + " '" + value + "' already exists");
    }
}

