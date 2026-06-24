package com.bank.onboarding.component;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for component tests. The dedicated test profile supplies an
 * isolated in-memory database, so subclasses only need to inherit the
 * Spring Boot context and MockMvc wiring.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractComponentTest {
}

