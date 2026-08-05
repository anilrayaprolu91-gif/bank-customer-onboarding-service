# Playwright Features & Framework Index
**A complete map of what each prompt teaches you about Playwright and enterprise test automation**

---

## 🎯 Playwright API Features Demonstrated

### Prompt 01-02: Setup & Configuration
| Feature | Code Example | Purpose |
|---------|------|---------|
| `APIRequestContext` | Playwright's built-in HTTP client | No external libraries (axios, node-fetch) |
| `playwright.config.ts` | Multi-project config | Different test suites (smoke, regression, contract) |
| `baseURL` env variable | Dynamic base URL injection | Same tests, different environments |
| Reporter setup | `list`, `allure`, `junit` | Multiple output formats simultaneously |
| Global setup/teardown | `globalSetup.ts`, `globalTeardown.ts` | Health checks before tests, cleanup after |
| Parallel execution | `workers: 4` | Run tests concurrently |
| Retries | `retries: 2` (CI only) | Flaky test resilience |

### Prompt 06: Fixtures (Dependency Injection)
| Feature | Code Example | Purpose |
|---------|------|---------|
| `test.extend()` | `extends base test` | Custom fixtures |
| Fixture scopes | `scope: 'function'` | Per-test isolation |
| Fixture composition | api.fixture extends base.fixture | Layered dependencies |
| `afterEach` hooks | `async cleanup()` | Automatic resource cleanup |
| Fixture teardown | Promise-based | No try/finally boilerplate |

### Prompt 11: Health Checks & Shell Execution
| Feature | Code Example | Purpose |
|---------|------|---------|
| `.get('/actuator/health')` | Built-in health check | Service readiness before tests |
| Response status codes | `response.status()` | Validate HTTP codes |
| JSON parsing | `response.json()` | Work with response data |
| Error handling | `throw` custom errors | Readable failure messages |

### Prompt 04: Core HTTP Methods
| Feature | Code Example | Purpose |
|---------|------|---------|
| `.get(url)` | Read data | No side effects |
| `.post(url, { body })` | Create resources | Payloads as JSON |
| `.put(url, { body })` | Full replacement | Idempotent updates |
| `.patch(url, { body })` | Partial update | Status changes, field updates |
| `.delete(url)` | Remove resources | Test cleanup |
| Headers | `.set({ 'X-Request-ID': uuid })` | Request tracing |
| Response headers | `response.headers()` | Validate response metadata |

### Prompt 07: Assertions & Matchers
| Feature | Code Example | Purpose |
|---------|------|---------|
| `expect(response).toBeTruthy()` | Basic assertions | HTTP status validation |
| `response.json()` chaining | `.json().then(data => {...})` | Type-safe response parsing |
| Custom matchers | `expect(response).toMatchSchema()` | Domain-specific assertions |
| Soft assertions | `expect.soft()` | Collect all failures before failing |
| Assertion errors | Detailed diff output | Debugging test failures |

### Prompt 17: Mocking & Route Interception
| Feature | Code Example | Purpose |
|---------|------|---------|
| `page.route(urlPattern, handler)` | Intercept HTTP | Mock external APIs |
| `route.abort('blockedbyclient')` | Fail requests | Simulate network errors |
| `route.fulfill({ status, body })` | Custom responses | Stub endpoints |
| `route.continue()` | Pass-through | Real requests after setup |
| **WireMock Admin API** | `.post('/__admin/mappings')` | Server-side stub control |

### Prompt 16: Timing & Performance
| Feature | Code Example | Purpose |
|---------|------|---------|
| `Date.now()` measurement | Measure request time | Response latency tracking |
| `response.headers()['x-response-time']` | Server timing | Trust server metrics |
| Timeout assertions | `expect(elapsed).toBeLessThan(2000)` | SLA validation |
| Concurrent requests | `Promise.all([...])` | Load testing patterns |

### Prompt 14: CI/CD Integration
| Feature | Code Example | Purpose |
|---------|------|---------|
| GitHub Actions trigger | `on: [push, pull_request]` | Auto-run tests |
| Artifact uploading | `actions/upload-artifact@v4` | Store reports for review |
| Matrix testing | `node-version: [18, 20, 22]` | Cross-version validation |
| Status checks | Fail workflow if tests fail | Block merges on red tests |
| Environment secrets | `secrets.STAGING_API_KEY` | Secure credential injection |

---

## 🏗️ Framework Patterns Demonstrated

### Pattern 1: Typed API Clients (Prompt 04)
**What you learn:**
- Encapsulation: Hide HTTP methods inside client classes
- Type safety: Every response is typed
- DRY principle: Endpoint paths are defined once
- Error handling: Consistent error shapes across tests

**Code structure:**
```typescript
// base-api-client.ts — abstract base
class BaseApiClient {
  protected async get<T>(path: string): Promise<ApiResponse<T>> { ... }
}

// customer-api-client.ts — domain-specific
class CustomerApiClient extends BaseApiClient {
  async onboardCustomer(req: CustomerOnboardingRequest): Promise<ApiResponse<CustomerResponse>> {
    return this.post('/api/v1/customers/onboard', req);
  }
}

// test.spec.ts — tests use the client
const response = await customerClient.onboardCustomer(validPayload);
expect(response.success).toBe(true);
```

**Why it matters:** Tests read like business language, not HTTP plumbing

---

### Pattern 2: Request Builders (Prompt 05)
**What you learn:**
- Fluent API design (chainable methods)
- Builder pattern (construct complex objects step-by-step)
- Deterministic test data (no randomness, full control)
- Self-documenting payloads

**Code structure:**
```typescript
const request = new CustomerOnboardingRequestBuilder()
  .withDefaults()
  .withHighRisk()
  .withMultipleAddresses()
  .build();
```

**Why it matters:** Non-developers can read what data each test uses

---

### Pattern 3: Fixtures as Dependency Injection (Prompt 06)
**What you learn:**
- Playwright's fixture system (cleaner than beforeEach/afterEach)
- Test isolation through separate `APIRequestContext` per test
- Fixture composition (base → api → wiremock)
- Automatic cleanup with `afterEach`

**Code structure:**
```typescript
// Define once
export const test = base.extend<{
  customerClient: CustomerApiClient;
  accountClient: AccountApiClient;
}>({
  customerClient: async ({ request, config }, use) => {
    const client = new CustomerApiClient(request, config);
    await use(client);
    // cleanup happens here
  },
});

// Use in tests: client is injected
test('should onboard', async ({ customerClient }) => {
  const response = await customerClient.onboardCustomer(payload);
  expect(response.success).toBe(true);
});
```

**Why it matters:** No setup boilerplate, tests focus on assertions

---

### Pattern 4: JSON Schema Validation (Prompt 07)
**What you learn:**
- Contract-driven testing (not just status codes)
- JSON Schema Draft-07 format (industry standard)
- AJV validator (fast, accurate validation)
- Custom Playwright matchers

**Code structure:**
```typescript
// Define schema once
// customer-response.schema.json
{
  "type": "object",
  "properties": {
    "id": { "type": "string", "pattern": "^[0-9a-f-]{36}$" },
    "email": { "type": "string", "format": "email" }
  },
  "required": ["id", "email"]
}

// Use in tests
const response = await customerClient.onboardCustomer(payload);
expect(response).toMatchSchema('customer-response');
```

**Why it matters:** Catch API changes before they break real clients

---

### Pattern 5: Test Data Registry & Cleanup (Prompt 18)
**What you learn:**
- Test isolation patterns (track created resources)
- Registry pattern (centralized cleanup state)
- Async cleanup with error tolerance
- Database hygiene (don't pollute test DB)

**Code structure:**
```typescript
// Register what gets created
test('should create customer', async ({ customerClient, testDataRegistry }) => {
  const response = await customerClient.onboardCustomer(payload);
  testDataRegistry.register('customer', response.id);
  // afterEach automatically cleans up
});
```

**Why it matters:** Tests don't interfere with each other, runs are repeatable

---

### Pattern 6: Multi-Environment Configuration (Prompt 13)
**What you learn:**
- Environment abstraction (no hardcoded URLs)
- dotenv for local/.env files
- Config singleton pattern
- Validation at startup

**Code structure:**
```typescript
// Config singleton
export const config = {
  baseUrl: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:8080',
  isCI: process.env.CI === 'true',
  logLevel: process.env.LOG_LEVEL || 'info',
};

// playwright.config.ts
if (config.isCI) {
  workers = 2; // CI has fewer resources
}
```

**Why it matters:** Same test code for dev, test, staging, prod

---

### Pattern 7: Allure Reporting (Prompt 12)
**What you learn:**
- Rich test reporting (not just pass/fail)
- Allure annotations (@epic, @feature, @story, @tag)
- Step tracking (see sub-actions in reports)
- Attachment support (attach JSON requests/responses)

**Code structure:**
```typescript
test('should create account', async ({ customerClient, allure }) => {
  await allure.step('Create customer', async () => {
    customer = await customerClient.onboardCustomer(payload);
  });
  
  await allure.step('Create account', async () => {
    account = await customerClient.createAccount(customer.id, accountPayload);
  });
  
  expect(account.status).toBe('OPEN');
});
```

**Why it matters:** Non-technical stakeholders understand test flows

---

### Pattern 8: Mocking Strategies (Prompt 17)
**What you learn:**
- **Playwright `route()`** — intercept browser/test calls
- **WireMock Admin API** — control what the app's dependencies return
- When to use each (test-layer vs server-layer mocking)

**Code structure - Playwright mocking:**
```typescript
test('should handle timeout', async ({ page }) => {
  await page.route('**/api/external/**', route => {
    route.abort('timedout');
  });
  // App now gets network error when calling external API
  const response = await customerClient.onboardCustomer(payload);
  expect(response.success).toBe(false);
});
```

**Code structure - WireMock Admin API:**
```typescript
test('should handle high-risk assessment', async ({ wiremock, customerClient }) => {
  // Tell the app's WireMock: "when risk API is called, return HIGH"
  await wiremock.stubPost('/api/risk/**', { riskScore: 95 });
  
  const response = await customerClient.onboardCustomer(payload);
  expect(response.riskLevel).toBe('HIGH');
  
  // Cleanup happens in afterEach
});
```

**Why it matters:** Different problems need different mocking strategies

---

### Pattern 9: Performance Baselines (Prompt 16)
**What you learn:**
- SLA assertions (response time <= X ms)
- Performance as a test concern (not a separate tool)
- Concurrent request patterns

**Code structure:**
```typescript
test('onboarding should respond within SLA', async ({ customerClient }) => {
  const startTime = Date.now();
  const response = await customerClient.onboardCustomer(payload);
  const elapsed = Date.now() - startTime;
  
  expect(elapsed).toBeLessThan(2000); // SLA: 2 seconds
});
```

**Why it matters:** Catch performance regressions automatically

---

## 📚 Learning Path by Role

### For QA Engineer
1. **Prompts 01-02** — Setup + config (30 min)
2. **Prompts 03-06** — Types + clients + fixtures (2 hours)
3. **Prompts 08-09** — Smoke + regression tests (2 hours)
4. **Prompts 07, 10** — Schema validation, contract tests (1 hour)

**Time: 5.5 hours**
**Outcome:** Can write 50+ tests, understand architecture

---

### For Automation Architect
1. **All of the above** (5.5 hours)
2. **Prompts 11, 13** — Docker integration, multi-env config (1 hour)
3. **Prompt 14** — GitHub Actions CI (1 hour)
4. **Prompts 16-18** — Performance, mocking, cleanup strategies (2 hours)

**Time: 9.5 hours**
**Outcome:** Can design test infrastructure for teams

---

### For Portfolio/Interview Prep
1. **Read this file** (15 min) — understand all patterns
2. **Skimread `PLAYWRIGHT_FRAMEWORK_PROMPTS.md`** (30 min) — see prompt structure
3. **Complete Prompts 01-10** in order (8 hours) — working implementation
4. **Complete Prompts 14-15** (2 hours) — CI/CD + polish
5. **Complete Prompt 16-18** (2 hours) — advanced patterns

**Time: 12.5 hours over 2 days**
**Outcome:** Production-grade portfolio project ready to show recruiters

---

## 🎓 Key Concepts You'll Master

### Concept 1: Test Architecture
**Learn from:** Prompts 02, 04, 06, 13
- Separation of concerns (clients, fixtures, config)
- Dependency injection (no global state)
- Layered architecture (fixtures → clients → tests)

**Interview question:** "How would you structure tests for a microservice with multiple endpoints?"

---

### Concept 2: Type Safety in Tests
**Learn from:** Prompts 03, 04, 07
- Strong typing across all layers (request → response)
- No `any` types (catch bugs at compile time)
- Type-safe schema validation

**Interview question:** "How do you prevent API contract changes from breaking tests?"

---

### Concept 3: Test Data Management
**Learn from:** Prompts 05, 18
- Builder pattern (fluent APIs)
- Deterministic data (reproducible tests)
- Cleanup strategies (isolation)

**Interview question:** "How do you handle test data when tests run in parallel?"

---

### Concept 4: Test Reporting & Analytics
**Learn from:** Prompts 12, 14, 16
- Rich reporting (not just pass/fail)
- Performance tracking (SLAs, baselines)
- CI/CD integration (visibility to stakeholders)

**Interview question:** "How do you report test results to non-technical stakeholders?"

---

### Concept 5: Enterprise Integration
**Learn from:** Prompts 11, 13, 14, 17
- Multi-environment support (dev, test, prod)
- CI/CD orchestration (GitHub Actions)
- Mocking at scale (Playwright + WireMock)
- Health checks & reliability

**Interview question:** "How would you run this framework in a CI/CD pipeline?"

---

## 🔗 Prompt Dependency Graph

```
Prompt 01 (Scaffolding)
  ↓
Prompt 02 (Directory Structure)
  ↓
Prompt 13 (Configuration) ← can be done anytime
  ↓
Prompts 03-04 (Types + Clients) ← foundation
  ↓
Prompts 05-06 (Builders + Fixtures) ← test structure
  ↓
Prompts 07, 11, 12 (Schema + Docker + Allure) ← quality gates
  ↓
Prompts 08-10 (Tests) ← implementation
  ↓
Prompts 14-15 (CI/CD + README) ← deployment
  ↓
Prompts 16-18 (Advanced) ← polish
```

**Do NOT skip early prompts** — later ones depend on earlier infrastructure.

---

## 📊 Coverage Map

After all 18 prompts, your framework covers:

| Aspect | Coverage |
|--------|----------|
| **Test Layers** | Smoke (5), Regression (17), Contract (4), Performance (5) = **31 tests** |
| **API Endpoints** | Onboarding (5), Account (4), Status (2), Health (1) = **12 endpoints** |
| **Error Scenarios** | Invalid email, missing fields, expired docs, 404s, 409s = **7+ negative cases** |
| **Mocking** | Playwright route(), WireMock Admin API = **2 strategies** |
| **Environments** | Local, CI, Staging = **3 profiles** |
| **Reporting** | Allure + JUnit XML + GitHub Pages = **3 formats** |
| **Test Data** | 4 named customers + builders + factories = **deterministic** |
| **Cleanup** | Registry + cleanup service + afterEach hooks = **enterprise-grade** |

---

## 💼 Interview Talking Points

### "What would you do differently than the prompts?"

You have options:
- Add UI tests (Playwright with `page` object model)
- Add API mock servers (WireMock standalone)
- Add load testing (Artillery alongside Playwright)
- Add visual regression (Playwright screenshots + Percy)
- Add accessibility testing (axe-core integration)

**Answer:** *"The framework is designed to be extensible. I chose [X] because [reason]. I'd add [Y] if we had [constraint]."*

---

### "How do you handle flaky tests?"

**Answer:** *"The framework is designed to minimize flakiness: deterministic data (no randomness), proper fixture isolation, health checks before tests, SLA baselines that alert on regressions, no sleeps (only waits). If flakiness occurs, it's a signal that the system has an issue."*

---

### "Tell me about test independence."

**Answer:** *"Each test gets its own APIRequestContext (Playwright fixture), its own test data via builders, and automatic cleanup via TestDataRegistry. Tests can run in parallel (workers: 4) without interference. Cleanup is logged, so failures are visible but never blocking."*

---

## 🚀 Next Steps After Portfolio Completion

1. **Host on GitHub** (not GitLab for maximum visibility)
2. **Enable GitHub Pages** (publish Allure reports)
3. **Add to resume** (with live link to Allure reports)
4. **Share in LinkedIn** (post a screenshot of the Allure dashboard)
5. **Write a blog post** (explain one pattern, e.g., "Why I Use Builders for Test Data")
6. **Apply to roles** (cite this project in cover letters)

---

## 📞 Questions While Building?

**Question:** "Prompt X says [feature], but I'm doing [other approach]. Is that okay?"

**Answer:** Follow the prompt first to understand the recommendation. Then iterate. Generic flexibility is fine; premature optimization is not.

---

**Good luck building your portfolio! 🎉**

*Next file to read: `/PLAYWRIGHT_FRAMEWORK_PROMPTS.md` (18 copy-paste prompts)*

