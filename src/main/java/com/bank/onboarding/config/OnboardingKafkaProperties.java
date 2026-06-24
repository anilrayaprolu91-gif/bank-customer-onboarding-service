package com.bank.onboarding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "onboarding.kafka")
public class OnboardingKafkaProperties {

    private boolean enabled = false;
    private String consumerGroup = "bank-customer-onboarding-service";
    private Topic topic = new Topic();

    @Data
    public static class Topic {
        private String customerOnboarded = "customer.onboarded.v1";
    }
}

