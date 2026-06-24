package com.bank.onboarding.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(UUID id) {
        super("Customer not found with id: " + id);
    }

    public CustomerNotFoundException(String customerNumber) {
        super("Customer not found with customer number: " + customerNumber);
    }
}

