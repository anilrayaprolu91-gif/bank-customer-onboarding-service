# Playwright Portfolio Project — One-Page Summary

## 🎯 PROJECT OVERVIEW
**Bank Customer Onboarding API Test Framework**  
Enterprise-grade REST API testing with Playwright + TypeScript  
**Repository:** `bank-onboarding-playwright-e2e`  
**Companion Service:** `bank-customer-onboarding-service` (Spring Boot)

---

## 📊 BY THE NUMBERS
| Metric | Value |
|--------|-------|
| **Test Cases** | 31+ (smoke, regression, contract, performance) |
| **Endpoints Covered** | 12 (customer, account, health) |
| **TypeScript Strictness** | 100% (zero `any` types) |
| **Prompts / Patterns** | 18 enterprise-grade design patterns |
| **Build Time** | 5-7 working days (start to finish) |
| **Test Execution** | ~5 minutes (smoke: <60s, regression: ~3min, contract: ~1min) |

---

## 🏗️ ARCHITECTURE AT A GLANCE

```
┌─────────────────────────────────────────────────────────┐
│                     PLAYWRIGHT TESTS                     │
│  /tests/smoke  /tests/regression  /tests/contract       │
└────────────────────────┬─────────────────────────────────┘
                         │
┌─────────────────────────▼─────────────────────────────────┐
│                    FIXTURES (Dependency Injection)         │
│  api.fixture.ts (customerClient, accountClient, cleanup)   │
└────────────────────────┬─────────────────────────────────┘
                         │
┌─────────────────────────▼──────────────────────────────────┐
│              API CLIENTS (Typed HTTP Wrappers)             │
│  CustomerApiClient  │  AccountApiClient  │  BaseApiClient  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌─────────────────────────▼──────────────────────────────────┐
│  DATA BUILDERS + FACTORIES + SCHEMA VALIDATORS             │
│  CustomerOnboardingRequestBuilder  │  TestDataFactory       │
│  SchemaValidator (JSON Draft-07)   │  PerformanceAsserter   │
└────────────────────────┬────────────────────────────────────┘
                         │
┌─────────────────────────▼──────────────────────────────────┐
│  SUPPORT LAYER (Config, Logger, Docker, Mocking)          │
│  config.ts  │  logger.ts  │  docker.ts  │  playwright-mock │
└────────────────────────┬────────────────────────────────────┘
                         │
┌─────────────────────────▼──────────────────────────────────┐
│  PLAYWRIGHT APIRequestContext  (HTTP Client)               │
│  (no external HTTP libraries — Playwright built-in)        │
└────────────────────────┬────────────────────────────────────┘
                         │
┌─────────────────────────▼──────────────────────────────────┐
│  Spring Boot REST API (running in Docker)                  │
│  http://localhost:8080  (postgres + kafka + app)           │
└──────────────────────────────────────────────────────────────┘
```

---

## 🎓 7 KEY PATTERNS YOU'LL LEARN

### 1. Type-Safe API Clients
```typescript
// Instead of: const data = await fetch(...).then(r => r.json())
const response = await customerClient.onboardCustomer(payload);
// Fully typed, no `any`
```
**Why:** Catch API changes at compile time, not at runtime

---

### 2. Fluent Request Builders
```typescript
const request = new CustomerOnboardingRequestBuilder()
  .withDefaults()             // Valid Australian customer
  .withHighRisk()             // Risk score > 80
  .withMultipleAddresses()    // 3 addresses
  .build();
```
**Why:** Tests read like specification, not code

---

### 3. Fixture-Based Dependency Injection
```typescript
test('should onboard', async ({ customerClient, logger, cleanup }) => {
  // No setup boilerplate, fixtures injected
  const response = await customerClient.onboardCustomer(payload);
  expect(response.success).toBe(true);
  // cleanup happens automatically after test
});
```
**Why:** Tests focus on assertions, not setup/teardown

---

### 4. JSON Schema Contract Validation
```typescript
expect(response).toMatchSchema('customer-onboard-response');
// Validates: structure, types, required fields, patterns, formats
// Same schemas used in Spring Cloud Contract (server-side)
```
**Why:** Catch breaking changes before they hit production

---

### 5. Multi-Environment Configuration
```powershell
# .env.local
PLAYWRIGHT_BASE_URL=http://localhost:8080
LOG_LEVEL=debug

# .env.ci
PLAYWRIGHT_BASE_URL=http://app:8080
LOG_LEVEL=info
CI=true
```
**Why:** Same tests run locally, CI, staging, and production

---

### 6. Mocking at Two Layers
```typescript
// Layer 1: Playwright route() — mock test-process calls
await page.route('**/external/**', route => route.abort());

// Layer 2: WireMock Admin API — mock server-side dependencies
await wiremock.stubPost('/api/risk/**', { riskScore: 95 });
```
**Why:** Different problems need different solutions

---

### 7. Enterprise Test Isolation + Cleanup
```typescript
test('should create customer', async ({ customerClient, testDataRegistry }) => {
  const customer = await customerClient.onboardCustomer(payload);
  testDataRegistry.register('customer', customer.id);
  // afterEach cleanup is automatic, logged, and never blocking
});
```
**Why:** Tests don't interfere, database doesn't grow unbounded

---

## 📚 PROMPT LIBRARY

**File:** `PLAYWRIGHT_FRAMEWORK_PROMPTS.md` (883 lines, 18 prompts)

| Prompt | Topic | Time |
|--------|-------|------|
| 01-02 | Scaffolding + structure | 30 min |
| 03-04 | Types + API clients | 1 hr |
| 05-06 | Builders + fixtures | 1 hr |
| 07, 11-12 | Schema validation, Docker, Allure | 1.5 hrs |
| 08-10 | Test implementation | 3 hrs |
| 13-14 | Config + CI/CD | 1.5 hrs |
| 16-18 | Performance, mocking, cleanup | 2 hrs |
| **Total** | | **11 hrs** |

**How to use:**
1. Create new repo: `bank-onboarding-playwright-e2e`
2. Open in VS Code with GitHub Copilot
3. Paste Prompt 01 into Copilot Chat
4. Copilot generates all code
5. Run commands to verify
6. Move to Prompt 02
7. Repeat

---

## 🚀 QUICK START (5 minutes)

```powershell
# Terminal 1: Start bank service
cd D:\bank-customer-onboarding-service
docker-compose up --build

# Terminal 2: Run tests
cd D:\bank-onboarding-playwright-e2e
npm ci
npm run test:smoke              # < 1 min
npm run report:open             # Opens Allure dashboard
```

---

## 📈 WHAT RECRUITERS WILL NOTICE

✅ **Type-safe testing** (strict TypeScript, zero `any`)  
✅ **Architecture thinking** (clients, fixtures, separation of concerns)  
✅ **Maintainability focus** (builders, schema validation, cleanup)  
✅ **Real-world patterns** (multi-env config, CI/CD, performance baselines)  
✅ **Enterprise practices** (Allure reporting, JUnit XML, GitHub Pages)  
✅ **Communication skills** (README, JSDoc, design decisions documented)

---

## 💡 INTERVIEW TALKING POINTS

**"Tell me about this project."**
> "I built a production-grade REST API test framework in Playwright + TypeScript. It covers 31+ test cases across smoke, regression, and contract testing, with schema validation, performance baselines, mocking strategies, and enterprise-grade cleanup. Tests are fully typed, fixtures provide dependency injection, and data is constructed via builders. Everything deploys to CI/CD with Allure reporting."

**"What's one pattern you're particularly proud of?"**
> "The fixture composition. By using Playwright's extend() API, I created a layered dependency injection system: base fixture provides config/logger/health check, API fixture adds typed clients, cleanup fixture handles test data registry. Tests are pure assertions — no setup boilerplate."

**"How do you handle test data?"**
> "Builders with defaults. CustomerOnboardingRequestBuilder.withDefaults() returns a valid Australian customer. Methods like withHighRisk() or withExpiredDocument() let tests specify exactly what variant they need. Data is deterministic, not random. Cleanup is automatic via TestDataRegistry."

**"What would you do differently?"**
> "The framework is extensible. I'd add UI tests (Playwright page object model), visual regression (Playwright screenshots + Percy), load testing (k6 alongside Playwright), or accessibility testing (axe-core). It depends on business needs."

---

## 📁 FOLDER STRUCTURE

```
bank-onboarding-playwright-e2e/
├── src/
│   ├── api/
│   │   ├── clients/              → Typed HTTP clients
│   │   │   ├── base-api-client.ts
│   │   │   ├── customer-api-client.ts
│   │   │   └── account-api-client.ts
│   │   └── schemas/              → JSON schemas (contract definitions)
│   │
│   ├── data/
│   │   ├── builders/             → Fluent builders for payloads
│   │   │   └── customer-onboarding-request.builder.ts
│   │   └── factories/            → Test data factories
│   │       └── test-data.factory.ts
│   │
│   ├── fixtures/                 → Dependency injection
│   │   ├── base.fixture.ts
│   │   ├── api.fixture.ts
│   │   └── wiremock.fixture.ts
│   │
│   ├── support/                  → Utilities
│   │   ├── config.ts             → Typed configuration
│   │   ├── logger.ts             → Structured logging
│   │   ├── docker.ts             → Health checks
│   │   ├── schema-validator.ts   → AJV validation
│   │   ├── playwright-mock.ts    → Route mocking
│   │   ├── wiremock-admin-client.ts
│   │   ├── test-data-registry.ts
│   │   └── cleanup.ts
│   │
│   └── types/
│       ├── api.types.ts          → API domain models
│       └── test.types.ts
│
├── tests/
│   ├── smoke/                    → @smoke tag (< 60s, critical paths)
│   │   └── customer-onboarding.smoke.spec.ts
│   ├── regression/               → @regression tag (comprehensive)
│   │   ├── customer-onboarding.regression.spec.ts
│   │   ├── performance-baseline.spec.ts
│   │   ├── playwright-native-mocking.spec.ts
│   │   └── server-side-dependency.spec.ts
│   └── contract/                 → @contract tag (API contracts)
│       └── customer-onboarding.contract.spec.ts
│
├── .github/
│   └── workflows/
│       └── playwright-e2e.yml    → GitHub Actions CI/CD
│
├── .env.local                    → Local: http://localhost:8080
├── .env.ci                       → CI: http://app:8080
├── .env.staging                  → Staging: https://staging.example
│
├── playwright.config.ts
├── tsconfig.json
├── package.json
├── README.md                     → Portfolio-grade documentation
├── docker-compose.test.yml       → Local Docker setup
├── allure.config.js              → Allure report configuration
│
└── scripts/
    ├── set-env.ps1              → PowerShell env switcher
    └── set-env.sh               → Bash env switcher
```

---

## 🔄 CI/CD FLOW

```
Push to GitHub
    ↓
GitHub Actions triggers playwright-e2e.yml
    ↓
Checkout app source + this test repo
    ↓
docker-compose up (app + postgres + kafka)
    ↓
Wait for health: GET /actuator/health → 200
    ↓
npm ci  →  npx playwright test --project=api-smoke
    ↓
✅ All smart? → Continue to regression
❌ Smoke failed? → Fail fast, alert developer
    ↓
Generate Allure report
    ↓
Upload to GitHub Pages: https://YOUR_USERNAME.github.io/bank-onboarding-playwright-e2e/
    ↓
docker-compose down
    ↓
Done!
```

---

## 📊 TEST METRICS

**After completing all 18 prompts:**

| Category | Count | Tags |
|----------|-------|------|
| Smoke Tests | 5 | @smoke (< 60s) |
| Happy-Path Regression | 12 | @regression (happy paths) |
| Negative/Error Tests | 7 | @regression (error cases) |
| Performance Baselines | 5 | @performance @regression |
| Contract Tests | 4 | @contract |
| Mocking Examples | 2 | @regression with mocks |
| Data Cleanup | Automatic | Tracked, logged, never blocking |
| **Total** | **31+** | |

---

## ☑️ FEATURE CHECKLIST

**Code Quality**
- [ ] 100% TypeScript strict mode
- [ ] Zero `any` types
- [ ] All public API has JSDoc
- [ ] Barrel exports in every folder
- [ ] No hardcoded credentials

**Testing**
- [ ] 5+ smoke tests (< 60s)
- [ ] 17+ regression tests
- [ ] 4+ contract tests
- [ ] 5 performance baseline tests
- [ ] 2+ mocking examples
- [ ] Parallel safe (workers: 4)

**Architecture**
- [ ] API clients (encapsulation)
- [ ] Builders (fluent, readable)
- [ ] Fixtures (DI, no globals)
- [ ] Schema validation (contract-driven)
- [ ] Test data registry (cleanup)

**Reporting**
- [ ] Allure results + HTML report
- [ ] JUnit XML (CI integration)
- [ ] GitHub Pages deployment
- [ ] Performance metrics
- [ ] Cleanup summary

**Documentation**
- [ ] README (quick-start + architecture)
- [ ] JSDoc (every public API)
- [ ] .env.example (no secrets)
- [ ] TROUBLESHOOTING section
- [ ] "Adding new tests" guide

---

## 🎁 BONUS: Interview Cheat Sheet

**Q: How do you prevent flaky tests?**  
A: Deterministic data (no randomness), fixture isolation (separate APIRequestContext per test), health checks before tests, no sleeps (Playwright waits), SLA baselines. Flakiness is a signal.

**Q: How do you ensure tests don't interfere?**  
A: Each test gets its own APIRequestContext fixture. TestDataRegistry tracks created resources. afterEach cleanup is automatic. Tests run in parallel (workers: 4) without interference.

**Q: How do you scale this to 100+ tests?**  
A: Parallel execution (Playwright handles it). Fixtures for dependency injection (no boilerplate). Builders for readable data. Schema validation (catch contract drift early). Performance baselines (alert on regressions).

**Q: What would be your next step beyond REST API tests?**  
A: Add Playwright page object model for UI tests. Add accessibility testing (axe-core). Add load testing (k6 in the same pipeline). Add visual regression (Percy snapshots). It's modular.

---

## 📞 Questions?

**Read in this order:**
1. This file (overview)
2. `FEATURE_INDEX_AND_PATTERNS.md` (deep patterns)
3. `PORTFOLIO_SHOWCASE_GUIDE.md` (build timeline + interview prep)
4. `PLAYWRIGHT_FRAMEWORK_PROMPTS.md` (18 copy-paste prompts)

---

**Good luck! You've got this. 🚀**

*Next: Start at Prompt 01 in `PLAYWRIGHT_FRAMEWORK_PROMPTS.md`*

