# Bank Customer Onboarding Service

A production-grade Spring Boot banking microservice and enterprise test automation showcase, designed to demonstrate Senior Test Automation Engineering capability for major Australian banking environments.

This project highlights architecture, quality engineering rigor, CI/CD controls, and risk-aware testing practices expected in regulated financial platforms.

---

## Portfolio Positioning

This repository is structured as an interview-ready implementation aligned with delivery standards typically required by institutions such as NAB, ANZ, Westpac, and Commonwealth Bank.

Key outcomes demonstrated:

- enterprise API test framework design (layered and extensible)
- CI/CD quality controls and branch governance
- measurable quality gates and report-driven feedback loops
- maintainable, scalable test architecture patterns
- production-like automation strategy for banking domains

---

## Technology Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Application Framework | Spring Boot 3.3.5 |
| Build Tool | Gradle 8 |
| API Testing | Rest Assured + JUnit 5 |
| Contract Testing | Spring Cloud Contract (provider) + Pact (consumer-driven) |
| Assertions | AssertJ |
| Serialization | Jackson |
| Schema Contracts | Rest Assured JSON Schema Validator |
| Mocking | WireMock |
| Environment Provisioning | Testcontainers |
| Quality Analysis | SonarQube, SpotBugs, JaCoCo |
| Reporting | JUnit XML, HTML reports, Allure results |
| Containerization | Docker, Docker Compose |
| CI/CD | Jenkins Declarative Pipeline |

---

## Framework Architecture

### 1) Business Service Architecture

Layered Spring Boot microservice design:

- `controller` - API contracts
- `service` - domain use-cases and orchestration
- `repository` - persistence abstraction
- `mapper` - DTO/domain mapping via MapStruct
- `exception` - centralized RFC 7807 error handling

This separation supports stable contracts, easier defect triage, and testability at different risk layers.

### 2) Test Automation Architecture

The automation design applies enterprise test layering:

- `test` - fast unit tests
- `componentTest` - component/spring-context verification
- `blackboxTest` - full API automation (Rest Assured)
- `consumerContractTest` - consumer-driven contracts (Pact)

Key automation packages (`src/apiTest/java/com/bank/onboarding/api`):

- `builder` - request builder pattern for deterministic and reusable payloads
- `factory` - factory pattern for client/spec/test data construction
- `client` - endpoint-oriented API clients (encapsulated HTTP interactions)
- `config` - environment profile resolution (`embedded`, `local`, `container`, `mock`)
- `filters` - logging and correlation controls
- `support` - container bootstrap, WireMock support, JSON utilities
- `model` - typed request/response contracts
- `tests` - behavior-focused blackbox scenarios

### 3) Design Patterns Used

- Request Builder Pattern
  - consistent, composable banking payload creation
- Factory Pattern
  - centralized construction of API clients/specs/test data
- Environment Profile Strategy
  - profile-based endpoint and infra switching without test rewrites

---

## Enterprise Testing Strategy

### Risk-Based Layering

- Unit tests detect logic regressions quickly
- Component tests validate wiring and cross-bean integration
- Blackbox tests validate externally observable behavior and contract integrity

### Blocking vs Non-Blocking Signals

In CI:

- Unit and Component are blocking quality gates
- Blackbox can be configured as non-blocking (UNSTABLE), enabling continuous feedback without unnecessary delivery stoppage

### Contract and API Quality Controls

- JSON schema validation for response contract integrity
- Spring Cloud Contract verification for provider-side API compatibility
- Pact contracts to encode consumer expectations against mock provider interactions
- typed Jackson mapping for strict API model verification
- centralized assertion style (AssertJ)

### Dependency Simulation and Isolation

- WireMock for deterministic dependency behavior
- Testcontainers profile available for production-like environment execution
- Embedded profile for fast deterministic CI execution

---

## CI/CD Implementation (Jenkins)

The `Jenkinsfile` uses a production-oriented declarative pipeline with branch-aware governance.

### Stage Flow

1. Checkout
2. Build
3. Test Execution (parallel):
   - Unit Test (blocking)
   - Component Test (blocking)
   - Blackbox Test (configurable non-blocking/UNSTABLE)
4. SonarQube
5. SonarQube Quality Gate
6. SpotBugs
7. Jacoco
8. Docker Build
9. Release Approval Gate (for `release/*`)
10. Docker Push (optional)
11. Quality Gate Summary
12. Publish Reports

### Governance Controls

- branch strategy (`main`, `develop`, `release/*`, PR-aware logic)
- release manual approval gate with restricted approvers
- optional docker publish with credentials binding
- report and artifact archiving for auditability

### Quality Gate Behavior

SonarQube / SpotBugs / JaCoCo can be skipped automatically when blocking test layers fail, while report publishing still executes to preserve triage visibility.

---

## Scalability and Maintainability

### Scalability

- layered source sets allow independent growth of test suites
- parallelized pipeline execution reduces feedback cycle time
- profile-driven execution supports local, embedded, and containerized environments
- client abstraction enables straightforward endpoint expansion

### Maintainability

- reusable builders and factories reduce duplication
- strongly typed request/response models reduce brittle test code
- centralized request specification and filters standardize diagnostics
- deterministic test data factories enable predictable runs

### Change Resilience

- schema validation catches contract drift early
- package structure supports team ownership boundaries
- test stage decomposition enables selective optimization without architecture changes

---

## Project Structure (Key Areas)

```text
src/
  main/java/com/bank/onboarding/
    controller/
    service/
    repository/
    mapper/
    exception/

  test/java/com/bank/onboarding/                 # unit tests
  componentTest/java/com/bank/onboarding/        # component tests
  apiTest/java/com/bank/onboarding/api/          # blackbox framework + tests

Jenkinsfile                                      # CI/CD pipeline
TEST-LAYERS.md                                   # layered test execution guide
```

---

## Running the Test Layers Locally

```powershell
Set-Location "D:\bank-customer-onboarding-service"
.\gradlew.bat test --no-daemon
.\gradlew.bat generateContractTests --no-daemon
.\gradlew.bat contractTest --no-daemon
.\gradlew.bat consumerContractTest --no-daemon
.\gradlew.bat componentTest --no-daemon
.\gradlew.bat blackboxTest --no-daemon
.\gradlew.bat testLayersReport --no-daemon
```

Contract-testing details: `CONTRACT_TESTING.md`

### Backward Compatibility Alias

```powershell
Set-Location "D:\bank-customer-onboarding-service"
.\gradlew.bat apiTest --no-daemon
```

---

## Reporting Outputs

- Unit reports: `build/reports/tests/test`
- Component reports: `build/reports/tests/componentTest`
- Blackbox reports: `build/reports/tests/blackboxTest`
- Consumer contract reports: `build/reports/tests/consumerContractTest`
- Aggregate layered report: `build/reports/tests/layers`
- Pact files (consumer side): `build/pacts`
- JaCoCo: `build/reports/jacoco/test`
- Allure results: `build/allure-results`

---

## API Documentation

When the service is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/api-docs`

---

## Why This Matters for Banking Delivery

This project demonstrates practical capability across the delivery lifecycle:

- robust API quality automation strategy
- CI governance and approval controls
- production-aware test stage orchestration
- maintainable framework design that scales with product complexity
- traceable quality evidence for release confidence

It is intentionally structured to communicate both hands-on engineering depth and quality leadership maturity expected at Senior Test Automation Engineer level.

