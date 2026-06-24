package com.bank.onboarding.event;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaTopicsProperties(
        boolean enabled,
        String customerOnboardedTopic,
        String consumerGroupId
) {
}

