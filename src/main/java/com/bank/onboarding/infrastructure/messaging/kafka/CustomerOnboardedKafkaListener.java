package com.bank.onboarding.infrastructure.messaging.kafka;

import com.bank.onboarding.event.ConsumedOnboardingEventStore;
import com.bank.onboarding.event.CustomerOnboardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class CustomerOnboardedKafkaListener {

    private final ConsumedOnboardingEventStore consumedOnboardingEventStore;

    @KafkaListener(
            topics = "${app.kafka.customer-onboarded-topic}",
            groupId = "${app.kafka.consumer-group-id}"
    )
    public void onCustomerOnboarded(CustomerOnboardedEvent event) {
        consumedOnboardingEventStore.record(event);
        log.info("Consumed CustomerOnboardedEvent from topic for customerId={}", event.customerId());
    }
}


