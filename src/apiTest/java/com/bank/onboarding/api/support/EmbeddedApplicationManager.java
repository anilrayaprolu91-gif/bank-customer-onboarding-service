package com.bank.onboarding.api.support;

import com.bank.onboarding.BankCustomerOnboardingApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public final class EmbeddedApplicationManager {

    private static final Object MONITOR = new Object();
    private static ConfigurableApplicationContext context;
    private static String baseUrl;

    private EmbeddedApplicationManager() {
    }

    public static String start() {
        synchronized (MONITOR) {
            if (context != null && context.isActive()) {
                return baseUrl;
            }

            context = new SpringApplicationBuilder(BankCustomerOnboardingApplication.class)
                    .profiles("test")
                    .run(
                            "--server.port=0",
                            "--spring.main.banner-mode=off"
                    );

            int port = ((ServletWebServerApplicationContext) context).getWebServer().getPort();
            baseUrl = "http://localhost:" + port;
            return baseUrl;
        }
    }
}

