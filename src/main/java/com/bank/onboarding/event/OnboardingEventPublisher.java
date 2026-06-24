package com.bank.onboarding.event;

public interface OnboardingEventPublisher {

    void publishCustomerOnboarded(CustomerOnboardedEvent event);
}

