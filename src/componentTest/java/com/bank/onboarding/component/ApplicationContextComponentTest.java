package com.bank.onboarding.component;

import com.bank.onboarding.BankCustomerOnboardingApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BankCustomerOnboardingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ApplicationContextComponentTest {

    @Test
    void shouldLoadApplicationContextForComponentLayer() {
        assertThat(true).isTrue();
    }
}

