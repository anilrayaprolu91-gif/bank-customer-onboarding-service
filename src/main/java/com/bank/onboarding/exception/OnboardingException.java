package com.bank.onboarding.exception;

public class OnboardingException extends RuntimeException {

    public OnboardingException(String message) {
        super(message);
    }

    public OnboardingException(String message, Throwable cause) {
        super(message, cause);
    }
}

