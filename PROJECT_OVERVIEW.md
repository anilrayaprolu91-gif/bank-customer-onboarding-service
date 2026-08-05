# PROJECT OVERVIEW

A step-by-step walkthrough to present this framework confidently in a technical interview or live screen share.

---

## Before You Start — 2-Minute Prep Checklist

```powershell
# Confirm Java 21 is available
java -version

# Confirm Docker is running (needed for blackbox embedded profile)
docker info

# Pre-warm the Gradle wrapper to avoid download pauses on screen share
Set-Location "D:\bank-customer-onboarding-service"
.\gradlew.bat --version --no-daemon
```

---

## STEP 1 — Set the Context (2 min)

**Intent:**

> "This is a Spring Boot microservice for banking customer onboarding — the same kind of domain you'd find at NAB, ANZ, or CBA.
> I built it as a full quality engineering showcase, not just a test script collection. 
> The intent was to demonstrate the thinking, architecture, and CI/CD governance expected at Senior level."

**Open in IDE and point to the top-level structure:**

```
src/
  main/           ← production microservice (Spring Boot 3, Java 21)
  test/           ← unit tests
  componentTest/  ← Spring context integration tests
  apiTest/        ← full blackbox API automation framework

Jenkinsfile       ← declarative CI/CD pipeline
build.gradle      ← custom Gradle source sets + quality toolchain
```

**Key talking point:** _"Everything is intentional. Three independent test source sets, each with its own Gradle task, classpath, and report output."_

---

## STEP 2 — Walk the Test Pyramid / Layering Strategy (3 min)

Open `TEST-LAYERS.md` and `build.gradle` side by side.

### Talk through each layer:

| Layer | Location | Purpose | CI Behaviour |
|---|---|---|---|
| `test` | `src/test` | Fast unit tests, no Spring context | **Blocking** — pipeline fails on red |
| `componentTest` | `src/componentTest` | Spring context wiring, H2 in-memory | **Blocking** — pipeline fails on red |
| `blackboxTest` | `src/apiTest` | Full API automation via Rest Assured | **Non-blocking** — UNSTABLE, not FAILURE |

**Point to `build.gradle` lines 185–224:**

```groovy
tasks.register('componentTest', Test) {
    shouldRunAfter tasks.named('test')   // ← explicit ordering
    ...
}

tasks.register('blackboxTest', Test) {
    shouldRunAfter tasks.named('componentTest')
    systemProperty 'api.profile', System.getProperty('api.profile', 'embedded')
    ...
}
```

**Key talking point:** _"The ordering is deliberate. Fast, deterministic tests fail fast before we spend time spinning up a full API stack. The blackbox layer is UNSTABLE rather than FAILURE in Jenkins — a conscious decision to keep the delivery pipeline moving while still surfacing signal."_

---

## STEP 3 — Walk the API Test Framework Architecture (5 min)

Open `src/apiTest/java/com/bank/onboarding/api/` in the IDE tree and walk each package:

### `builder/` — Request Builder Pattern
Open `CustomerOnboardingRequestBuilder.java`:

```java
CustomerOnboardingRequestBuilder.aCustomer()
    .withEmail("jane.smith@example.com")
    .withInitialDeposit(new BigDecimal("5000.00"))
    .build();
```

**Say:** _"Fluent builders mean every test creates explicit, readable payloads. No magic maps, no copy-paste JSON strings. When a field changes in the API contract, you fix it in one place."_

---

### `factory/` — Factory Pattern
Open `TestDataFactory.java` and `ApiClientFactory.java`:

**Say:** _"Factories centralise construction. TestDataFactory gives every test a deterministic starting point. ApiClientFactory wires up the client to the correct environment profile without the test caring which environment it's running in."_

---

### `client/` — Endpoint-Oriented API Clients
Open `CustomerApiClient.java`:

**Say:** _"HTTP interactions are fully encapsulated in typed clients. Tests call `customerApiClient.onboardCustomer(request)` — not raw RestAssured specs. This means tests read as business behaviour, not HTTP mechanics. Endpoint changes are a one-file fix."_

---

### `config/` — Environment Profile Strategy
Open `EnvironmentConfigResolver.java` and `ApiProfile.java`:

**Say:** _"Four profiles: `embedded`, `local`, `container`, `mock`. The test switches profile via a system property. The same test code runs locally with WireMock, in CI with an embedded Spring Boot instance, or against a running container — zero test rewrites required."_

```powershell
# Embedded (default for CI)
.\gradlew.bat blackboxTest --no-daemon

# Local running service
.\gradlew.bat blackboxTest -Dapi.profile=local --no-daemon
```

---

### `filters/` — Correlation and Logging
Open `CorrelationIdFilter.java`:

**Say:** _"Every API request carries a correlation ID injected at the filter level. This mirrors what production observability platforms expect and makes test failure triage trivial — you can match a failed assertion to an exact request in logs."_

---

### `support/` — Infrastructure Management
Open `EmbeddedApplicationManager.java` and `ContainerizedApplicationManager.java`:

**Say:** _"The framework boots the entire application under test programmatically — either as an embedded Spring Boot context or a Testcontainers Docker container with a real PostgreSQL database. The test doesn't care which. This is what separates a framework from a test script."_

---

## STEP 4 — Run a Test Live (5 min)

### 4a. Unit tests (fastest)
```powershell
Set-Location "D:\bank-customer-onboarding-service"
.\gradlew.bat test --no-daemon
```
Open the report: `build/reports/tests/test/index.html`

---

### 4b. Component tests
```powershell
.\gradlew.bat componentTest --no-daemon
```
Open the report: `build/reports/tests/componentTest/index.html`

---

### 4c. Blackbox / API tests (the showcase layer)
```powershell
.\gradlew.bat blackboxTest --no-daemon
```
Open the report: `build/reports/tests/blackboxTest/index.html`

**Point out:**
- Allure annotations: `@Epic`, `@Feature`, `@Description`, `@Owner` visible in test output
- JSON schema validation happening alongside field-level AssertJ assertions
- Multi-step scenario: onboard → retrieve → update status → verify onboarding status

---

### 4d. Aggregate report across all layers
```powershell
.\gradlew.bat testLayersReport --no-daemon
```
Open: `build/reports/tests/layers/index.html`

**Say:** _"One aggregate view across all three layers. This is what I'd publish as a CI build artifact for the team and for release sign-off evidence."_

---

## STEP 5 — Show the Flagship Test Scenario (3 min)

Open `CustomerOnboardingApiTest.java`:

**Walk through the test method `shouldOnboardAndActivateCustomer()`:**

1. Creates request via `TestDataFactory` (factory pattern)
2. Calls `customerApiClient.onboardCustomer()` (client abstraction)
3. Validates **HTTP 201** status code
4. Validates **JSON schema** via `JsonSchemaValidator.matchesJsonSchemaInClasspath()`
5. Deserialises response into typed `CustomerResponse` record
6. Asserts business fields with **AssertJ** (readable, IDE-friendly)
7. Retrieves customer by ID — validates consistency
8. Retrieves customer by number — validates alternate lookup
9. Updates status → asserts `ACTIVE` lifecycle
10. Gets onboarding status → validates `eligibleForServices` flag

**Say:** _"This is a complete E2E customer journey in a single test method. It validates schema contracts AND business behaviour AND lifecycle state transitions — the kind of coverage that gives genuine release confidence in a regulated banking domain."_

---

## STEP 6 — Walk the Jenkins CI/CD Pipeline (3 min)

Open `Jenkinsfile`:

**Highlight each concern:**

### Parallel test execution (lines 58–112)
```groovy
parallel {
    stage('Unit Test')      { catchError(buildResult: 'FAILURE') ... }
    stage('Component Test') { catchError(buildResult: 'FAILURE') ... }
    stage('Blackbox Test')  { catchError(buildResult: 'UNSTABLE') ... }
}
```
**Say:** _"Unit and Component run in parallel. Blackbox is parallel too but deliberately UNSTABLE — it gives signal without blocking the pipeline."_

---

### Quality gate cascade (lines 114–170)
- SonarQube only runs if unit AND component tests pass — no wasted analysis on broken builds
- SpotBugs static analysis with MAX effort
- JaCoCo code coverage report
- Branch-gated: only `main`, `develop`, `release/*`, and PRs trigger the full chain

---

### Release Approval Gate (lines 188–205)
```groovy
stage('Release Approval Gate') {
    input(
        message: "Approve Docker push for ${env.EFFECTIVE_DOCKER_IMAGE_TAG}?",
        submitter: params.RELEASE_APPROVERS
    )
}
```
**Say:** _"Manual approval gate restricted to named release managers before any Docker push on a release branch. This is the kind of governance control that regulated environments require."_

---

### Audit trail (lines 251–256)
```groovy
archiveArtifacts artifacts: 'build/reports/**, build/test-results/**, build/allure-results/**, build/libs/*.jar'
```
**Say:** _"Reports, test results, and Allure data are all archived as build artifacts. Every release has a traceable quality evidence trail."_

---

## STEP 7 — Quality Toolchain Summary (1 min)

| Tool | What it shows |
|---|---|
| **JaCoCo** | Code coverage measurement and XML export for SonarQube |
| **SonarQube** | Static analysis, smell detection, coverage gate |
| **SpotBugs** (MAX effort) | Bytecode-level bug pattern detection |
| **Allure** | Behaviour-annotated HTML test reports |
| **WireMock** | Deterministic dependency simulation |
| **Testcontainers** | Real PostgreSQL in Docker for container profile |

---

## STEP 8 — Recruiter Questions You Should Expect

| Question | Your Answer |
|---|---|
| _"Why three test layers?"_ | Unit tests give sub-second feedback on logic. Component tests validate Spring wiring without network. Blackbox tests validate externally observable behaviour. Each layer has different risk, cost, and speed tradeoffs — you align them to the delivery risk model. |
| _"Why is blackbox UNSTABLE not FAILURE?"_ | Blackbox tests depend on infra (containers, DB) which can have transient failures unrelated to code quality. Blocking the pipeline on infra noise wastes engineer time. UNSTABLE surfaces the signal while allowing delivery to continue. |
| _"How does it scale to a larger API surface?"_ | New endpoints = new method in the typed client + new test. Builder handles payload variation. Factory handles data. Environment config handles where it runs. The framework grows linearly without architectural changes. |
| _"How do you handle test data in a banking context?"_ | Deterministic factories with explicit TINs, document numbers, and deposit amounts. No random data that creates flaky assertions. WireMock for dependency isolation means no shared state across test runs. |
| _"How do you handle environment differences?"_ | Profile strategy. One system property (`api.profile`) switches between embedded, local, container, and mock. Tests are environment-agnostic. |
| _"What does the correlation ID filter do?"_ | Injects a traceable ID into every request header. In a real banking platform, this threads through logs, APM tools, and audit trails. It's also the first thing you use to triage a failed test — grep the correlation ID in logs. |

---

## Key Architecture Phrases to Use

Use these when speaking to senior engineers or architects:

- _"Risk-based test layering aligned to the cost of feedback"_
- _"Environment profile strategy — same tests, zero rewrites across environments"_
- _"Endpoint-oriented client abstraction for maintainability at scale"_
- _"Builder pattern for deterministic, composable payload construction"_
- _"Schema + field assertion dual-layer contract validation"_
- _"Non-blocking quality signal vs blocking quality gate — a deliberate CI governance choice"_
- _"Traceable quality evidence trail for regulated release sign-off"_

---

## Quick Reference — Key Files to Open

| File | What to demonstrate |
|---|---|
| `src/apiTest/.../tests/CustomerOnboardingApiTest.java` | Full E2E scenario, schema + assertion layers |
| `src/apiTest/.../builder/CustomerOnboardingRequestBuilder.java` | Builder pattern |
| `src/apiTest/.../factory/TestDataFactory.java` | Factory pattern |
| `src/apiTest/.../client/CustomerApiClient.java` | Client abstraction |
| `src/apiTest/.../config/EnvironmentConfigResolver.java` | Profile strategy |
| `src/apiTest/.../filters/CorrelationIdFilter.java` | Observability integration |
| `src/apiTest/.../support/EmbeddedApplicationManager.java` | Framework bootstrapping |
| `Jenkinsfile` | CI/CD pipeline governance |
| `build.gradle` | Multi-source-set toolchain configuration |
| `build/reports/tests/layers/index.html` | Aggregate test evidence |

---

_This guide is structured for a 20-minute live technical walkthrough. Steps 1–3 are architecture discussion, Step 4 is live execution, Steps 5–6 are deep-dive, Step 7–8 are wrap-up._

---

## STEP 6 — Kafka Event-Driven Resilience & Feature Toggles (3 min)

Open `src/apiTest/java/com/bank/onboarding/api/tests/` and show both Kafka tests:

### Architecture Context

**Say:** _"The onboarding service publishes a domain event after customer creation. In production, downstream systems (AML, compliance, risk engines) consume these events asynchronously. But Kafka is optional — we can toggle it off for backward-compatibility or performance testing."_'

---

### Test 1: Positive Flow with Kafka Enabled (Event-Driven Path)
Open `KafkaOnboardingBlackboxApiTest.java`:

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class KafkaOnboardingBlackboxApiTest {
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(...);
    
    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("app.kafka.enabled", () -> "true");
        // ...
    }
    
    @Test
    void shouldOnboardCustomerAndConsumeKafkaEventInSingleFlow() {
        // Calls API, validates 201 + schema
        // Then: await consumed event in store
        // Assert: customer ID, email, number match
    }
}
```

**Say:** _"This test is Docker-backed. Testcontainers spins a real Kafka broker. The test calls the onboarding API, validates the response, then waits for the produced event to be consumed by a listener. This validates the full async event pipeline in a single integration flow."_

---

### Test 2: Negative Flow with Kafka Disabled (Fallback Path)
Open `KafkaDisabledOnboardingBlackboxApiTest.java`:

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = "app.kafka.enabled=false")
class KafkaDisabledOnboardingBlackboxApiTest {
    
    @Test
    void shouldOnboardCustomerSuccessfullyWhenKafkaDisabledAndEmitNoConsumedEvent() {
        // Calls API, validates 201 + schema (same as above)
        // Then: wait and assert NO event was consumed
    }
}
```

**Say:** _"Same API call, same assertions on the HTTP response. But Kafka is disabled via a property. The onboarding still succeeds, but the event listener never fires. This proves the system degrades gracefully when Kafka is offline — the critical path (customer creation) is not blocked by an optional event broker."_

---

### Run the Tests Live

```powershell
# Positive: Kafka enabled, event consumed
.\gradlew.bat blackboxTest --tests "*KafkaOnboardingBlackboxApiTest" --no-daemon

# Negative: Kafka disabled, no event consumed
.\gradlew.bat blackboxTest --tests "*KafkaDisabledOnboardingBlackboxApiTest" --no-daemon
```

**Say:** _"Both tests pass. The first proves the event pipeline works end-to-end. The second proves API resilience when Kafka is unavailable. This is how you design fault-tolerant event-driven systems — optional infrastructure never blocks critical business transactions."_

---

### Key Architecture Pattern: Feature Toggle Testing

**Say:** _"This is a practical example of feature toggle testing at scale. The `app.kafka.enabled` property controls a whole subsystem. The test suite validates both states. In production, you could toggle Kafka on/off for A/B testing, gradual rollout, or incident recovery without redeploying. The tests give you confidence in both modes."_'

**Recruiter talking points:**
- _"Event-driven architecture with graceful degradation"_
- _"Feature toggles + integration tests for safe operational changes"_
- _"Same API contract, two different event streams — proves separation of concerns"_
- _"Docker-backed Testcontainers tests vs. lightweight Spring Boot tests — tradeoff between realism and speed"_
