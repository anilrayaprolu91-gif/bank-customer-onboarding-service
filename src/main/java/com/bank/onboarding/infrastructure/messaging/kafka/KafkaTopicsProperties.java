package com.bank.onboarding.infrastructure.messaging.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaTopicsProperties(
        boolean enabled,
        String customerOnboardedTopic,
        String consumerGroupId
) {
}


