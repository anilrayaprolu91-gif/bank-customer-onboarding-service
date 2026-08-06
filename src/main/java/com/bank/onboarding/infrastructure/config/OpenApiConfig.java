package com.bank.onboarding.infrastructure.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI bankOnboardingOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development"),
                        new Server().url("https://api.bank.com").description("Production")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Bank Customer Onboarding Service API")
                .description("""
                        Production-grade REST API for banking customer onboarding.
                        
                        **Key capabilities:**
                        - Full customer profile creation with KYC document management
                        - AML / risk assessment integration
                        - Multi-address management per customer
                        - Bank account opening workflow
                        - Onboarding status lifecycle tracking
                        
                        All endpoints follow RFC 7807 (Problem Details) for error responses.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Platform Engineering")
                        .email("platform-engineering@bank.com")
                        .url("https://developers.bank.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://bank.com/terms"));
    }
}


