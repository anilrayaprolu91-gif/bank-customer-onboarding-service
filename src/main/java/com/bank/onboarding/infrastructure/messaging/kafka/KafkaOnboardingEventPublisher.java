package com.bank.onboarding.infrastructure.messaging.kafka;

import com.bank.onboarding.event.CustomerOnboardedEvent;
import com.bank.onboarding.event.OnboardingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class KafkaOnboardingEventPublisher implements OnboardingEventPublisher {

    private final KafkaTemplate<String, CustomerOnboardedEvent> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Override
    public void publishCustomerOnboarded(CustomerOnboardedEvent event) {
        kafkaTemplate.send(kafkaTopicsProperties.customerOnboardedTopic(), event.customerId(), event);
        log.info("Published CustomerOnboardedEvent to topic={} customerId={}",
                kafkaTopicsProperties.customerOnboardedTopic(), event.customerId());
    }
}


