# Playwright Enterprise Framework — Prompt Library

Use these prompts **in order** inside a new GitHub repo.
Each prompt is self-contained and builds on the previous output.
Target stack: **TypeScript + Playwright + Allure + Docker Desktop**.

---

## HOW TO USE

1. Create a new GitHub repo: `bank-onboarding-playwright-e2e`
2. Open it in VS Code or JetBrains with Copilot enabled
3. Paste each prompt into the Copilot Chat panel in sequence
4. After each prompt, run the generated commands to verify before moving on

---

## PROMPT 01 — Project Scaffolding

```
Create a new enterprise-grade Playwright automation framework in TypeScript for a banking REST API service.

Project name: bank-onboarding-playwright-e2e

Requirements:
- TypeScript strict mode
- Playwright Test runner (latest stable)
- Allure reporter for test results
- ESLint + Prettier for code quality
- dotenv for environment configuration
- Node 20+

Generate:
1. package.json with all dependencies and scripts:
   - test:local        → runs against localhost:8080 (Docker Desktop)
   - test:smoke        → tag @smoke only
   - test:regression   → full suite
   - test:report       → opens allure report
   - lint              → ESLint check
   - lint:fix          → auto-fix

2. playwright.config.ts with:
   - baseURL from env var PLAYWRIGHT_BASE_URL (default http://localhost:8080)
   - timeout: 30 seconds per test
   - retries: 2 on CI, 0 locally
   - reporter: list on local, allure + junit on CI
   - globalSetup and globalTeardown hooks
   - three named projects: api-smoke, api-regression, api-contract

3. tsconfig.json with strict settings

4. .env.example with:
   PLAYWRIGHT_BASE_URL=http://localhost:8080
   API_VERSION=v1
   TEST_TIMEOUT_MS=30000
   LOG_LEVEL=info

5. .gitignore including node_modules, allure-results, test-results, .env

6. README.md with "Getting Started" section explaining Docker Desktop prerequisite

Print every file with its full content.
```

---

## PROMPT 02 — Directory Structure & Barrel Exports

```
I have a Playwright TypeScript project for testing a banking REST API.

Set up the enterprise folder structure below. Create each folder and an index.ts barrel export file per folder. Do not generate test files yet.

Required structure:
src/
  api/
    clients/          ← typed HTTP client wrappers per resource
    schemas/          ← JSON schema definitions for response validation
    contracts/        ← Pact-style contract objects (expected shapes)
  fixtures/
    base.fixture.ts   ← extends Playwright test with shared setup
    api.fixture.ts    ← injects typed API clients into tests
  data/
    builders/         ← builder pattern for request payloads
    factories/        ← factory functions producing test data
    seeds/            ← static reference datasets (currencies, countries, etc.)
  support/
    config.ts         ← reads .env and exports typed config object
    logger.ts         ← structured console logger with log levels
    docker.ts         ← utility to health-check Docker app before tests start
    schema-validator.ts ← wrapper around ajv for response schema validation
  types/
    api.types.ts      ← TypeScript interfaces matching the service API models
    test.types.ts     ← shared types used across fixtures and tests
tests/
  smoke/
  regression/
  contract/
utils/
  http-status.ts      ← enum of expected HTTP status codes
  date-utils.ts       ← date helpers for test data
  assertion-helpers.ts← custom assertion wrappers

For each file, provide the complete implementation, not just stubs.
Start with: src/support/config.ts, src/support/logger.ts, src/types/api.types.ts
```

---

## PROMPT 03 — TypeScript API Types (Banking Domain)

```
I am building a Playwright API test framework for a bank customer onboarding REST service.

The service exposes these endpoints:
  POST /api/v1/customers/onboard
  GET  /api/v1/customers/{id}
  GET  /api/v1/customers/number/{customerNumber}
  PUT  /api/v1/customers/{id}/status
  GET  /api/v1/customers/{id}/onboarding-status
  POST /api/v1/customers/{id}/accounts
  GET  /api/v1/customers/{id}/accounts
  GET  /api/v1/customers/{id}/accounts/{accountId}
  POST /api/v1/customers/{id}/accounts/{accountId}/close

The onboarding request payload contains:
- personalInfo: { firstName, lastName, dateOfBirth (YYYY-MM-DD), nationality (2-char ISO), taxIdentificationNumber, email, phoneNumber (+E.164) }
- addresses[]: { addressType (HOME|WORK|MAILING), street, city, state, postalCode, country, isPrimary }
- kycDocuments[]: { documentType (PASSPORT|DRIVERS_LICENSE|NATIONAL_ID), documentNumber, issuingAuthority, issuingCountry, issueDate, expiryDate }
- riskProfile: { riskLevel (LOW|MEDIUM|HIGH), riskScore (0-100), assessedBy, factors[]: { factorName, factorDescription, weight } }
- initialAccount: { accountType (SAVINGS|CHECKING|TERM_DEPOSIT), currency (ISO 4217), initialDeposit, productCode }

The standard API response wrapper is:
  { success: boolean, message: string, data: T, timestamp: string }

Generate complete TypeScript interfaces in src/types/api.types.ts covering:
1. All request types (CustomerOnboardingRequest, AccountCreationRequest, UpdateStatusRequest)
2. All response types (CustomerResponse, AccountResponse, OnboardingStatusResponse)
3. The ApiResponse<T> generic wrapper
4. All enum types as TypeScript const enums
5. Error response type matching RFC 7807 (title, detail, status, type, violations[])

Use strict types — no `any`. Export everything.
```

---

## PROMPT 04 — Typed API Client Layer

```
I have TypeScript interfaces for a bank customer onboarding API in src/types/api.types.ts.

Build an enterprise-grade API client layer in src/api/clients/ using Playwright's APIRequestContext.

Requirements:
1. Create a base class BaseApiClient with:
   - constructor takes Playwright APIRequestContext and config
   - private helper methods: get(), post(), put(), patch(), delete()
   - each helper returns typed ApiResponse<T>
   - logs request method + path + status using the project logger
   - throws typed ApiError when response status >= 400
   - includes request ID header (X-Request-ID: uuid) on every call
   - includes Accept: application/json and Content-Type: application/json by default

2. Create CustomerApiClient extends BaseApiClient with methods:
   - onboardCustomer(request: CustomerOnboardingRequest): Promise<ApiResponse<CustomerResponse>>
   - getCustomerById(id: string): Promise<ApiResponse<CustomerResponse>>
   - getCustomerByNumber(customerNumber: string): Promise<ApiResponse<CustomerResponse>>
   - updateCustomerStatus(id: string, status: CustomerStatus): Promise<ApiResponse<CustomerResponse>>
   - getOnboardingStatus(id: string): Promise<ApiResponse<OnboardingStatusResponse>>

3. Create AccountApiClient extends BaseApiClient with methods:
   - createAccount(customerId: string, request: AccountCreationRequest): Promise<ApiResponse<AccountResponse>>
   - getAccountsForCustomer(customerId: string): Promise<ApiResponse<AccountResponse[]>>
   - getAccountById(customerId: string, accountId: string): Promise<ApiResponse<AccountResponse>>
   - closeAccount(customerId: string, accountId: string): Promise<ApiResponse<void>>

4. Create ApiClientFactory that returns fully configured client instances

All methods must be fully typed — no `any`. Include JSDoc on public methods.
Show complete file content for all files created.
```

---

## PROMPT 05 — Request Builder Pattern

```
I am building a Playwright TypeScript test framework for a banking API.

Implement a request builder pattern in src/data/builders/ for constructing test payloads.

Requirements:
1. CustomerOnboardingRequestBuilder:
   - fluent API: withPersonalInfo(), withAddress(), withKycDocument(), withRiskProfile(), withInitialAccount()
   - withDefaults() method returns a fully populated valid Australian customer
   - withInvalidEmail(), withMissingFirstName(), withExpiredDocument() for negative test cases
   - build() returns CustomerOnboardingRequest
   - each builder method validates its input and throws descriptive errors

2. AccountCreationRequestBuilder:
   - withAccountType(), withCurrency(), withInitialDeposit(), withProductCode()
   - withDefaults() returns a valid SAVINGS account in AUD
   - build() returns AccountCreationRequest

3. Create a TestDataFactory in src/data/factories/ that:
   - generates realistic Australian test data using static deterministic values (no random — tests must be reproducible)
   - provides namedCustomers map: STANDARD_CUSTOMER, HIGH_RISK_CUSTOMER, MINOR_CUSTOMER (under 18), DUPLICATE_EMAIL_CUSTOMER
   - provides named accounts: SAVINGS_ACCOUNT, CHECKING_ACCOUNT, LARGE_DEPOSIT_ACCOUNT
   - includes a uniqueEmail(prefix: string) helper that appends a timestamp suffix for isolation

Show complete implementations for all files. Use strict TypeScript — no any.
```

---

## PROMPT 06 — Playwright Fixtures

```
I have API clients and data builders for a Playwright TypeScript banking test framework.

Create the fixture layer in src/fixtures/:

1. base.fixture.ts:
   - extends Playwright's base test
   - provides: config, logger, dockerHealthCheck
   - dockerHealthCheck fixture calls GET /actuator/health on baseURL before any test
   - if the health check fails, throw a clear error: "Docker service not available at <url> — is docker-compose up?"
   - logs test name at start and pass/fail at end

2. api.fixture.ts:
   - extends base.fixture
   - provides: customerClient, accountClient, apiClientFactory
   - each client is scoped to the test (separate APIRequestContext per test for isolation)
   - includes a cleanupCreatedCustomers fixture that tracks customer IDs created during the test and can optionally call a delete endpoint if available

3. Export a merged `test` and `expect` from src/fixtures/index.ts so tests import from one place:
   import { test, expect } from '../../src/fixtures'

Requirements:
- Fixtures must be typed — no any
- Each fixture must have a JSDoc comment explaining its purpose
- Fixture teardown (after each) must log whether cleanup succeeded or was skipped

Show complete file content for all three files.
```

---

## PROMPT 07 — JSON Schema Validation

```
I am building a Playwright API test framework for a banking service.

Create a schema validation layer in src/api/schemas/ and src/support/schema-validator.ts.

Requirements:

1. Create JSON Schema files (JSON format, Draft-07) for:
   - customer-onboard-response.schema.json  → validates CustomerResponse data shape
   - account-response.schema.json           → validates AccountResponse data shape
   - onboarding-status-response.schema.json → validates OnboardingStatusResponse
   - error-response.schema.json             → validates RFC 7807 error shape
   - api-wrapper.schema.json                → validates the { success, message, data, timestamp } envelope

   Each schema must use: required fields, type constraints, pattern validation for IDs (UUID regex), email format, date-time format, enum values where applicable.

2. Create src/support/schema-validator.ts:
   - uses ajv (Another JSON Schema Validator) with draft-07
   - exports a SchemaValidator class with:
     - validate(schemaName: string, data: unknown): ValidationResult
     - assertValid(schemaName: string, data: unknown): void  ← throws with detailed error message if invalid
   - preloads all schemas at startup for performance
   - ValidationResult: { valid: boolean, errors: ValidationError[] }

3. Add a Playwright custom expect matcher in src/support/expect-extensions.ts:
   - expect(response).toMatchSchema('customer-onboard-response')
   - expect(response).toHaveStatus(201)
   - expect(response).toHaveValidUUID('id')

Show complete content for every file.
```

---

## PROMPT 08 — Smoke Tests

```
I have a fully typed Playwright TypeScript framework for a bank customer onboarding API.

Write smoke tests in tests/smoke/customer-onboarding.smoke.spec.ts

The app runs at http://localhost:8080 via Docker Desktop (docker-compose up).

Smoke tests must:
- be tagged @smoke
- cover only the critical happy-path journeys (not edge cases)
- use the custom test fixture (import { test, expect } from '../../src/fixtures')
- use CustomerOnboardingRequestBuilder.withDefaults() for payloads
- use the typed CustomerApiClient and AccountApiClient
- validate HTTP status codes, response schema, and key field values
- be fast: total smoke suite should complete in under 60 seconds

Write these 5 smoke tests:

1. should_onboard_new_customer_successfully
   - POST /api/v1/customers/onboard with valid payload
   - assert 201, success:true, customerStatus=PENDING_VERIFICATION
   - validate response matches customer-onboard-response schema
   - assert returned customerId is a valid UUID
   - assert customerNumber matches pattern CUST-\d{4}-\d{8}

2. should_retrieve_customer_by_id
   - onboard a customer, then GET /api/v1/customers/{id}
   - assert 200, email matches what was submitted

3. should_retrieve_customer_by_number
   - onboard, then GET /api/v1/customers/number/{customerNumber}
   - assert 200

4. should_create_account_for_customer
   - onboard customer, POST /api/v1/customers/{id}/accounts
   - assert 201, accountType and currency match request

5. should_return_health_ok
   - GET /actuator/health
   - assert 200, status=UP

Each test must have a clear describe block, Allure annotations (@allure.epic, @allure.feature, @allure.story), and use soft assertions where multiple field checks are needed.

Show complete file content.
```

---

## PROMPT 09 — Regression Tests (Happy Path + Negative)

```
I have a Playwright TypeScript framework with working smoke tests for a bank onboarding API.

Write regression tests in tests/regression/customer-onboarding.regression.spec.ts

Tag all tests @regression.

Include these test groups:

GROUP 1 — Onboarding Happy Path Variations
1. should_onboard_customer_with_multiple_addresses
2. should_onboard_customer_with_multiple_kyc_documents
3. should_onboard_high_risk_customer (riskLevel=HIGH, riskScore>80)
4. should_onboard_customer_with_checking_account
5. should_onboard_customer_with_term_deposit

GROUP 2 — Status Management
6. should_update_customer_status_to_active
7. should_retrieve_onboarding_status_after_onboarding
8. should_not_allow_invalid_status_transition (assert 400 or 422)

GROUP 3 — Account Management
9. should_get_all_accounts_for_customer
10. should_get_account_by_id
11. should_close_account_successfully
12. should_not_create_account_for_nonexistent_customer (assert 404)

GROUP 4 — Validation / Negative
13. should_reject_onboarding_with_invalid_email (assert 400, validate error shape)
14. should_reject_onboarding_with_missing_required_fields (assert 400)
15. should_reject_onboarding_with_expired_kyc_document
16. should_return_404_for_unknown_customer_id
17. should_return_409_for_duplicate_email (onboard same email twice)

Requirements:
- Each test is isolated (fresh customer per test via fixture)
- Negative tests assert: HTTP status, error.title, error.detail contains useful message
- Use assertj-style soft assertions via expect.soft()
- Include Allure step annotations using allure.step() for multi-step tests
- Add a beforeAll that logs: "Starting regression suite against <baseURL>"

Show complete file content.
```

---

## PROMPT 10 — Contract Validation Tests

```
I have a Playwright TypeScript framework for a banking API.

Write API contract tests in tests/contract/customer-onboarding.contract.spec.ts

These tests validate the HTTP contract (status codes, headers, response schema, field presence) independently of business logic. They are the Playwright-side complement to the Spring Cloud Contract tests on the server.

Tag all tests @contract.

Write tests for:

1. CONTRACT: POST /api/v1/customers/onboard
   - response Content-Type must be application/json
   - response body must match customer-onboard-response.schema.json
   - 201 status on valid request
   - response must contain: id (UUID), customerNumber (CUST-pattern), email, customerStatus, timestamp
   - 400 on invalid email
   - 400 on missing personalInfo
   - error response must match error-response.schema.json (RFC 7807)

2. CONTRACT: GET /api/v1/customers/{id}
   - 200 on existing customer
   - 404 on nonexistent UUID
   - 400 on malformed UUID

3. CONTRACT: POST /api/v1/customers/{id}/accounts
   - 201 on valid request
   - response must contain: accountId (UUID), accountType, currency, accountStatus
   - 404 when customerId does not exist

4. CONTRACT: GET /actuator/health
   - 200, body contains status: UP

Each test must:
- assert HTTP status code
- assert Content-Type header
- assert response schema using the custom toMatchSchema() matcher
- include an Allure annotation: @allure.tag('contract')

Show complete file content.
```

---

## PROMPT 11 — Docker Desktop Integration Utility

```
I am building a Playwright TypeScript test framework.
The Spring Boot app is started via Docker Desktop using docker-compose.

Create src/support/docker.ts with:

1. A DockerHealthChecker class:
   - constructor(baseUrl: string, maxWaitMs: number = 60000, pollIntervalMs: number = 2000)
   - waitUntilHealthy(): Promise<void>
     → polls GET /actuator/health until { status: "UP" } or throws after maxWaitMs
     → logs each attempt with elapsed time
   - isHealthy(): Promise<boolean>  → single check, no retries

2. A DockerComposeHelper class:
   - static getComposeInfo(): { projectName: string, services: string[] }
     → reads docker-compose.yml from project root (two levels up from src/)
     → parses service names using js-yaml
   - static printDockerContext(): void
     → prints BASE_URL, DOCKER_HOST env, and service list to console

3. A globalSetup.ts at the project root:
   - imports DockerHealthChecker
   - reads PLAYWRIGHT_BASE_URL from env
   - calls waitUntilHealthy() before any tests run
   - prints "✅ Service ready at <url>" on success
   - prints the docker-compose service list if available

4. A globalTeardown.ts at the project root:
   - logs "Test run complete. Allure results at: allure-results/"
   - prints a summary of environment vars used (mask passwords)

Wire globalSetup and globalTeardown into playwright.config.ts.

Show complete file content for all files.
```

---

## PROMPT 12 — Allure Reporting Setup

```
I have a Playwright TypeScript framework using allure-playwright reporter.

Configure enterprise-grade Allure reporting:

1. Update playwright.config.ts reporter section to output:
   - allure-results/ directory
   - junit-results/junit.xml (for CI JUnit parsing)
   - HTML report in playwright-report/

2. Create src/support/allure-helpers.ts with a typed wrapper:
   - step(name: string, fn: () => Promise<void>): Promise<void>
   - attachment(name: string, content: string, type: 'application/json' | 'text/plain'): void
   - label(name: string, value: string): void
   - addRequestAttachment(method: string, url: string, body: unknown, response: unknown): void
     → attaches request + response as formatted JSON to the Allure report

3. Create an allure.config.js at project root for the CLI:
   - resultsDir: allure-results
   - reportDir: allure-report
   - links: { issue: { urlTemplate: 'https://jira.bank.internal/browse/%s' } }
   - environment info: APP_VERSION, BASE_URL, TEST_ENV

4. Add to package.json scripts:
   - report:generate  → allure generate allure-results --clean -o allure-report
   - report:open      → allure open allure-report
   - report:ci        → report:generate (no open, for CI)

5. Create a test-helpers/attach-response.ts utility that:
   - takes an Playwright APIResponse
   - attaches: status, headers (filtered), and body as JSON to Allure
   - call this in base fixture afterEach for failed tests automatically

Show all files with complete implementations.
```

---

## PROMPT 13 — Environment Configuration (Local vs CI)

```
I have a Playwright TypeScript test framework.

Implement a multi-environment configuration system:

1. Create environment config files:
   .env.local       → PLAYWRIGHT_BASE_URL=http://localhost:8080, LOG_LEVEL=debug
   .env.ci          → PLAYWRIGHT_BASE_URL=http://app:8080, LOG_LEVEL=info, CI=true
   .env.staging     → PLAYWRIGHT_BASE_URL=https://staging.bank.internal, LOG_LEVEL=warn

2. Create src/support/config.ts that exports a typed Config object:
   interface Config {
     baseUrl: string
     apiVersion: string
     timeoutMs: number
     retries: number
     logLevel: 'debug' | 'info' | 'warn' | 'error'
     isCI: boolean
     dockerHost: string
   }

   - reads from process.env (populated by dotenv)
   - validates required fields exist, throws if baseUrl is missing
   - exports a singleton: export const config = loadConfig()

3. Update playwright.config.ts to:
   - import config and set: baseURL, timeout, retries, reporter dynamically based on config.isCI
   - use: workers: config.isCI ? 2 : 4

4. Create scripts/set-env.ps1 (PowerShell) and scripts/set-env.sh (bash):
   - accept an argument: local | ci | staging
   - copy the matching .env.<env> to .env
   - print "Environment set to: <env>"

5. Update README.md with a "Running Against Different Environments" section

Show complete file content for all files. Include the README section inline.
```

---

## PROMPT 14 — GitHub Actions CI Workflow

```
I have a Playwright TypeScript framework that tests a Spring Boot service running in Docker.

Create a GitHub Actions workflow file at .github/workflows/playwright-e2e.yml

Requirements:

1. Trigger on:
   - push to main and develop
   - pull_request targeting main
   - workflow_dispatch (manual with inputs: environment, test_tag)

2. Job: e2e-tests
   - runs-on: ubuntu-latest
   - services:
     - postgres: postgres:15 with POSTGRES_DB=onboarding, POSTGRES_USER=postgres, POSTGRES_PASSWORD=postgres
     - app: use docker-compose to build and start the Spring Boot app
       (checkout the bank-customer-onboarding-service repo as a sibling step)
   - steps:
     a. Checkout this repo
     b. Checkout the app repo (bank-customer-onboarding-service) to ./app-source
     c. Build and start services: cd app-source && docker-compose up -d --build
     d. Wait for health: curl --retry 10 --retry-delay 5 http://localhost:8080/actuator/health
     e. Setup Node 20
     f. npm ci
     g. Run tests: npx playwright test --project=api-regression (or smoke on PR)
     h. Upload allure-results as artifact
     i. Upload junit-results as artifact (always, even on failure)
     j. Generate Allure report and upload allure-report artifact
     k. Docker teardown: cd app-source && docker-compose down

3. Add a separate job: publish-report
   - depends on e2e-tests (runs always)
   - deploys allure-report to GitHub Pages (gh-pages branch)
   - only on main branch

4. Add environment secrets documentation in README:
   Required secrets: none (all defaults for local Docker)
   Optional: STAGING_BASE_URL, STAGING_API_KEY

Show the complete .github/workflows/playwright-e2e.yml file.
```

---

## PROMPT 15 — README for the Playwright Repo

```
I have built an enterprise Playwright TypeScript API test framework for a bank customer onboarding service.

Write a complete README.md for the GitHub repo bank-onboarding-playwright-e2e.

Include these sections:

1. Project Overview
   - What this framework tests
   - Relationship to the bank-customer-onboarding-service repo
   - Technology stack table (Playwright, TypeScript, Allure, Docker, GitHub Actions)

2. Prerequisites
   - Node 20+
   - Docker Desktop installed and running
   - Clone the bank-customer-onboarding-service repo alongside this one
   - Windows (PowerShell) and macOS/Linux instructions where they differ

3. Quick Start (5 steps)
   Step 1: Clone both repos side by side
   Step 2: Start the app via Docker Desktop
   Step 3: Install dependencies
   Step 4: Run smoke tests
   Step 5: Open the Allure report

4. Running Tests
   - All local commands (powershell fenced blocks)
   - Environment switching (local / ci / staging)
   - Running by tag (@smoke, @regression, @contract)
   - Running a single test file

5. Framework Architecture
   - Folder structure diagram (tree format)
   - Layer explanation: fixtures → clients → builders → tests
   - Design decisions (why builder pattern, why schema validation, why fixture isolation)

6. Reporting
   - How to generate and open Allure report
   - Where JUnit XML is output (for CI)
   - Screenshot and trace on failure (explain Playwright trace viewer)

7. CI/CD
   - GitHub Actions workflow explanation
   - GitHub Pages report publishing
   - How to trigger manually

8. Adding New Tests
   - Step-by-step guide: add a new endpoint, add types, add client method, add builder, write test
   - Code snippets for each step

9. Troubleshooting
   - App not healthy: check docker-compose up, check port 8080
   - Tests timing out: increase TEST_TIMEOUT_MS
   - Schema validation failing: regenerate schemas from OpenAPI spec

Make the README professional and recruiter-readable. Include badges: Node version, Playwright version, CI status.
```

---

## PROMPT 16 — Advanced: Performance Baseline Tests

```
I have a Playwright TypeScript API test framework for a bank onboarding service.

Add performance baseline tests in tests/regression/performance-baseline.spec.ts

These are NOT load tests — they are single-request SLA baseline assertions that run as part of regression.

Requirements:
- tag all tests @performance @regression
- use Playwright's built-in timing (response.headers()['x-response-time'] if available, or measure via Date.now())

Write these tests:

1. onboarding_endpoint_should_respond_within_2000ms
   - POST /api/v1/customers/onboard
   - assert response time <= 2000ms
   - assert 201

2. get_customer_by_id_should_respond_within_500ms
   - GET /api/v1/customers/{id}
   - assert response time <= 500ms

3. create_account_should_respond_within_1500ms
   - POST /api/v1/customers/{id}/accounts
   - assert response time <= 1500ms

4. health_check_should_respond_within_200ms
   - GET /actuator/health
   - assert response time <= 200ms

5. concurrent_onboarding_requests_should_all_succeed
   - send 5 concurrent onboarding requests using Promise.all()
   - assert all return 201
   - assert all complete within 5000ms total

Create a PerformanceAsserter helper in src/support/performance-asserter.ts that:
- wraps APIResponse with timing info
- provides assertResponseTime(actual: number, slaMs: number, testName: string): void
- logs: "PERF: <testName> responded in <actual>ms (SLA: <sla>ms) [PASS/FAIL]"

Show complete file content.
```

---

## PROMPT 17 — Mocking Strategy: Playwright Native vs WireMock

> **Decision guide (read before generating code):**
>
> ✅ Use **Playwright's built-in `route` API** when:
>   - You are writing UI/browser tests and want to intercept calls the BROWSER makes to the API
>   - You want to simulate network errors, timeouts, or inject delays in client-side tests
>   - You want to replay recorded traffic from a HAR file
>
> ✅ Use the **WireMock Admin API** (via an HTTP client in Playwright) when:
>   - The **Spring Boot app itself** calls an external service server-side (e.g. a risk-reference API)
>   - WireMock is already embedded in the Spring Boot app (as it is in this project)
>   - You need to control what the Spring Boot app's WireMock instance returns BEFORE you call the onboarding endpoint
>
> ❌ Do NOT use Playwright route() to intercept server-to-server calls — Playwright only intercepts network traffic from the browser or the Playwright test process, not from the Spring Boot JVM.

```
I have a Playwright TypeScript framework that tests a bank onboarding REST API running in Docker.

The Spring Boot service has WireMock embedded (accessible at http://localhost:8080/__admin) to stub external dependencies like a risk-reference API.

I need two separate mocking approaches:

--- APPROACH A: Playwright native route() mocking ---
This is for UI or test-process-level request interception.

1. Create src/support/playwright-mock.ts:
   - PlaywrightMockBuilder class that wraps page.route() with typed helpers
   - mockApiResponse(page: Page, urlPattern: string, responseBody: unknown, status: number): Promise<void>
   - mockNetworkFailure(page: Page, urlPattern: string): Promise<void>
   - mockSlowResponse(page: Page, urlPattern: string, delayMs: number, body: unknown): Promise<void>
   - captureRequests(page: Page, urlPattern: string): RequestCapture
     → returns an object with .getAll() returning captured request bodies

2. Write tests in tests/regression/playwright-native-mocking.spec.ts:
   - test: simulate downstream timeout when calling onboarding — verify graceful error response
   - test: mock an external health-check endpoint to return DOWN — verify app degrades gracefully
   - Use page.route() / request.fulfill() / request.abort() patterns
   - Tag: @mock @regression

--- APPROACH B: WireMock Admin API (server-side stub control) ---
This controls what the Spring Boot app's embedded WireMock instance returns.

3. Create src/support/wiremock-admin-client.ts:
   - WireMockAdminClient class using Playwright APIRequestContext (no extra library needed)
   - constructor(adminUrl: string = 'http://localhost:8080/__admin')
   - stubGet(urlPattern: string, responseBody: unknown, status: number): Promise<string>  → returns stub ID
   - stubPost(urlPattern: string, responseBody: unknown, status: number): Promise<string>
   - removeStub(stubId: string): Promise<void>
   - resetAll(): Promise<void>
   - getRequestsMatching(urlPattern: string): Promise<RecordedRequest[]>
   - verifyCalledTimes(urlPattern: string, expectedTimes: number): Promise<void>

4. Create src/fixtures/wiremock.fixture.ts:
   - extends api.fixture
   - provides wiremock: WireMockAdminClient
   - afterEach: calls resetAll() automatically so stubs never leak between tests

5. Write tests in tests/regression/server-side-dependency.spec.ts:
   - test: stub risk-reference service to return HIGH risk score
     → POST onboarding → verify customer is created with riskLevel HIGH
     → verify stub was called exactly once via verifyCalledTimes()
   - test: stub risk-reference to return 503
     → POST onboarding → verify app returns appropriate error or degrades
   - test: reset stub → verify normal flow works again
   - Tag: @wiremock @regression

Important implementation notes:
// WireMock admin API is available only when the Spring Boot app runs in embedded/local profile.
// In docker-compose the app exposes /__admin on port 8080 because WireMock is embedded.
// Do NOT use WireMock for intercepting calls between your test process and the API.
// For that, use Playwright's native route() API.

Show complete file content for all 5 files with JSDoc comments explaining which mocking approach is used and why.
```

---

## PROMPT 18 — Advanced: Test Data Cleanup Strategy

```
I have a Playwright API test framework for a banking service. Tests create customers and accounts that persist in the database.

Implement an enterprise-grade test data cleanup strategy:

1. Create src/support/test-data-registry.ts:
   - TestDataRegistry singleton class
   - register(type: 'customer' | 'account', id: string): void
   - getAll(type): string[]
   - clear(): void

2. Create src/support/cleanup.ts:
   - CleanupService class
   - constructor takes CustomerApiClient
   - cleanupCustomers(ids: string[]): Promise<CleanupResult>
   - CleanupResult: { cleaned: string[], skipped: string[], errors: string[] }
   - logs each cleanup action
   - Note: if DELETE endpoint not available, log "Soft cleanup: recorded for manual review"

3. Update api.fixture.ts:
   - register created customer IDs in TestDataRegistry during the test
   - afterAll (suite level): run CleanupService.cleanupCustomers()
   - log cleanup summary

4. Create a cleanup report in afterAll global teardown:
   - print table: Test Name | Created Resources | Cleaned | Status
   - save to test-results/cleanup-report.txt

5. Add an npm script:
   - cleanup:report → cat test-results/cleanup-report.txt

This strategy ensures tests are isolated, the database does not grow unbounded, and cleanup failures are visible but not blocking.

Show complete implementation.
```

---

## QUICK REFERENCE — Run Order for Local Setup

After completing all prompts, this is the daily developer workflow:

```powershell
# Terminal 1 — start the Spring Boot app
Set-Location "D:\bank-customer-onboarding-service"
docker-compose up --build

# Terminal 2 — run tests (once app is healthy)
Set-Location "D:\bank-onboarding-playwright-e2e"
npm ci
npm run test:smoke          # < 60 seconds, critical paths only
npm run test:regression     # full suite
npm run report:generate
npm run report:open         # opens Allure in browser
```

---

## TECH STACK SUMMARY

| Concern | Choice | Why |
|---|---|---|
| Test runner | Playwright Test | Built-in HTTP client, fixtures, parallelism, tracing |
| Language | TypeScript strict | Type safety across API shapes, no silent contract drift |
| Assertions | Playwright expect + custom matchers | Schema validation inline with test assertions |
| Reporting | Allure + JUnit XML | Allure for rich HTML demo; JUnit for Jenkins/GitHub Actions |
| Request builders | Builder pattern | Readable, reusable, deterministic payloads |
| API clients | Typed wrappers over APIRequestContext | Single source of truth for endpoint paths and response types |
| Schema validation | AJV (JSON Schema Draft-07) | Same schema format used in the Spring Boot blackbox tests |
| Environment config | dotenv + typed Config singleton | Works local, Docker, CI without test rewrites |
| CI | GitHub Actions | Free, PR integration, GitHub Pages for Allure hosting |
| **Test-side mocking** | **Playwright `page.route()` / `request.fulfill()`** | **Native, zero-dependency, intercepts browser + test-process calls** |
| **Server-side stub control** | **WireMock Admin API (HTTP calls from Playwright)** | **Controls what the Spring Boot app's embedded WireMock returns — no extra library** |

### Mocking Decision Tree

```
Do you want to mock a call that...

  ...the BROWSER or your TEST PROCESS makes to an API?
      → Use Playwright route() / request.fulfill()    ← no WireMock needed

  ...the SPRING BOOT APP makes to an external service (server-to-server)?
      → Use WireMock Admin API via HTTP from your Playwright test
         (POST http://localhost:8080/__admin/mappings)
         Playwright's APIRequestContext is all you need — no wiremock npm package required
```

---

*This prompt library was generated for: bank-onboarding-playwright-e2e*
*Companion service: bank-customer-onboarding-service*
*Date: June 2026*

