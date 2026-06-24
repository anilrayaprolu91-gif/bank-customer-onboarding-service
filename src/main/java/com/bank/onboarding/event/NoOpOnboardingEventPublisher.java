package com.bank.onboarding.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class NoOpOnboardingEventPublisher implements OnboardingEventPublisher {

    @Override
    public void publishCustomerOnboarded(CustomerOnboardedEvent event) {
        log.debug("Kafka publishing is disabled. Skipping onboarded event for customerId={}", event.customerId());
    }
}

