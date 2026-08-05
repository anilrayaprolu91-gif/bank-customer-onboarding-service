# Three-Repo Testing — Execution Matrix & Quick Reference

Visual guide showing test execution flows, timings, and success criteria.

---

##  LOCAL SETUP — QUICK REFERENCE

### Start Everything Locally (3 Terminals)

```
┌─────────────────────────────────────────────────────────────────────┐
│ TERMINAL 1: Backend                                                  │
├─────────────────────────────────────────────────────────────────────┤
│ $ cd bank-customer-onboarding-service                                │
│ $ docker-compose up --build                                          │
│                                                                       │
│ ✅ Output: "Tomcat started on port 8080"                             │
│ Health check: curl http://localhost:8080/actuator/health             │
│ Swagger UI: http://localhost:8080/swagger-ui.html                    │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ TERMINAL 2: API Tests (REST Assured)                                │
├─────────────────────────────────────────────────────────────────────┤
│ $ cd bank-onboarding-blackbox-api                                    │
│ $ ./gradlew smokeTest -Dapi.baseUrl=http://localhost:8080 --no-daemon
│                                                                       │
│ ⏱️  Duration: ~5-8 minutes                                           │
│ ✅ Success: All tests passed, report at:                             │
│    build/reports/allure/index.html                                   │
│                                                                       │
│ Full regression (optional):                                          │
│ $ ./gradlew blackboxTest -Dapi.baseUrl=http://localhost:8080         │
│ ⏱️  Duration: ~15-20 minutes                                         │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ TERMINAL 3: UI Tests (Playwright)                                   │
├─────────────────────────────────────────────────────────────────────┤
│ $ cd bank-onboarding-ui-e2e                                          │
│ $ npm ci                                                              │
│ $ npm run test:smoke -- --baseURL http://localhost:8080              │
│                                                                       │
│ ⏱️  Duration: ~5-10 minutes                                          │
│ ✅ Success: All tests passed, report at:                             │
│    playwright-report/index.html                                      │
│                                                                       │
│ Full regression (optional):                                          │
│ $ npm run test:e2e -- --baseURL http://localhost:8080                │
│ ⏱️  Duration: ~10-15 minutes                                         │
└─────────────────────────────────────────────────────────────────────┘

TOTAL LOCAL TEST TIME: ~30-50 minutes (all three test suites)
```

---

##  PIPELINE EXECUTION FLOW

### GitHub Actions: On Push to Main

```
Developer: git push origin main
    │
    ▼
┌───────────────────────────────────┐
│ BACKEND CI/CD (bank-customer....) │  ⏱️ ~15 min
├───────────────────────────────────┤
│ ✅ Unit tests                      │
│ ✅ Component tests                 │
│ ✅ Build JAR                       │
│ ✅ Build Docker image              │
│ ✅ Push to ghcr.io                 │
│  repository_dispatch             │
└───────────────────────────────────┘
    │
    ├─➜ [blocks] API Tests
    │
    ▼
┌───────────────────────────────────┐
│ API TESTS (bank-onboarding-...)   │  ⏱️ ~10-15 min
├───────────────────────────────────┤
│ ✅ Pull backend image from registry│
│ ✅ Start backend (Docker)          │
│ ✅ Wait for health check           │
│ ✅ Run smoke tests                 │
│ ✅ Run regression tests (main only)│
│ ✅ Generate Allure reports         │
│  repository_dispatch (if pass)   │
└───────────────────────────────────┘
    │
    ├─➜ [blocks if main] UI Tests
    │
    ▼
┌───────────────────────────────────┐
│ UI TESTS (bank-onboarding-ui...)  │  ⏱️ ~10-15 min
├───────────────────────────────────┤
│ ✅ Install deps                    │
│ ✅ Pull backend image              │
│ ✅ Start backend (Docker)          │
│ ✅ Wait for health check           │
│ ✅ Run smoke tests                 │
│ ✅ Run regression tests (main only)│
│ ✅ Generate Playwright reports     │
│  Status: PASS/FAIL               │
└───────────────────────────────────┘
    │
    ▼
GitHub PR/Commit: ✅ All checks passed
(Reports available as artifacts)
```

### GitHub Actions: On Pull Request

```
Developer: git push origin feature-branch && create PR
    │
    ▼
┌───────────────────────────────────┐
│ BACKEND CI/CD                      │  ⏱️ ~10 min
├───────────────────────────────────┤
│ ✅ Unit tests (FAST)               │
│ ✅ Component tests (FAST)          │
│ ⏭️  Skip blackbox (not needed)     │
│ ✅ Build Docker image              │
└───────────────────────────────────┘
    │
    ▼
┌───────────────────────────────────┐
│ API TESTS                          │  ⏱️ ~5 min
├───────────────────────────────────┤
│ ✅ Run SMOKE only (faster feedback)│
│ ⏭️  Skip regression                │
└───────────────────────────────────┘
    │
    ▼
┌───────────────────────────────────┐
│ UI TESTS                           │  ⏱️ ~5 min
├───────────────────────────────────┤
│ ✅ Run SMOKE only (faster feedback)│
│ ⏭️  Skip regression                │
└───────────────────────────────────┘
    │
    ▼
PR Status: ✅ All checks passed (fast feedback ~20 min)
Developer can merge after review
```

---

##  TEST EXECUTION MATRIX

| Layer | Test Type | Duration | Success Gate | Run Condition |
|-------|-----------|----------|---|---|
| **Backend** | Unit | 2-3 min | BLOCKING | Always |
| | Component | 3-5 min | BLOCKING | Always |
| **API Tests** | Smoke | 5-8 min | BLOCKING (main) | Always |
| | Regression | 15-20 min | NON-BLOCKING | Main branch only |
| **UI Tests** | Smoke | 5-10 min | BLOCKING (main) | After API pass |
| | Regression | 10-15 min | NON-BLOCKING | Main branch only |

---

##  LOCAL TEST COMMANDS — COPY & PASTE

### Quick Smoke Test (Total: ~15 minutes)

```bash
# Terminal 1: Start backend
cd bank-customer-onboarding-service
docker-compose up --build

# Terminal 2: API smoke tests only
cd ../bank-onboarding-blackbox-api
./gradlew smokeTest -Dapi.baseUrl=http://localhost:8080 --no-daemon

# Terminal 3: UI smoke tests only
cd ../bank-onboarding-ui-e2e
npm ci
npm run test:smoke -- --baseURL http://localhost:8080

# View reports
echo "API Report:" && open ../bank-onboarding-blackbox-api/build/reports/allure/index.html
echo "UI Report:" && cd bank-onboarding-ui-e2e && npx playwright show-report
```

### Full Regression (Total: ~50 minutes)

```bash
# Terminal 1: Start backend
cd bank-customer-onboarding-service
docker-compose up --build

# Terminal 2: Full API tests
cd ../bank-onboarding-blackbox-api
./gradlew blackboxTest -Dapi.baseUrl=http://localhost:8080 --no-daemon

# Terminal 3: Full UI tests
cd ../bank-onboarding-ui-e2e
npm run test:e2e -- --baseURL http://localhost:8080

# View reports
open ../bank-onboarding-blackbox-api/build/reports/allure/index.html
cd bank-onboarding-ui-e2e && npx playwright show-report
```

### Single Test File (Debug)

```bash
# API: Single test
cd bank-onboarding-blackbox-api
./gradlew test --tests "*CustomerOnboardingSmokeTest" --no-daemon

# UI: Single test
cd bank-onboarding-ui-e2e
npx playwright test --grep @smoke
npx playwright test --debug  # interactive mode
```

---

##  DOCKER DESKTOP REQUIREMENTS

Before running tests locally:

```bash
# 1. Verify Docker is running
docker ps
# Should list running containers (or be empty)

# 2. Verify image access
docker pull postgres:15-alpine
# Should work without errors

# 3. Verify network
docker run --rm alpine ping -c 1 8.8.8.8
# Should respond

# 4. Free up resources
# If tests are slow, check Docker resources:
# - Docker Desktop settings → Resources
# - CPU: 4+ cores recommended
# - Memory: 8GB+ available
# - Disk: 20GB+ free space
```

---

## ✅ SUCCESS CHECKLIST — LOCAL RUN

After running all three test suites locally, verify:

```
Backend (Terminal 1)
  ✅ docker-compose up completed without errors
  ✅ curl http://localhost:8080/actuator/health returns {"status":"UP"}
  ✅ Postgres and Kafka containers running

API Tests (Terminal 2)
  ✅ ./gradlew smokeTest completed
  ✅ All tests passed (green checkmarks)
  ✅ Allure report generated at: build/reports/allure/index.html
  ✅ Report shows: X tests passed, 0 failed

UI Tests (Terminal 3)
  ✅ npm run test:smoke completed
  ✅ All tests passed (green checkmarks)
  ✅ Playwright report generated at: playwright-report/index.html
  ✅ Report shows: X tests passed, 0 failed, 0 skipped

Portfolio-Ready
  ✅ All three repos cloned locally
  ✅ All workflows pass locally
  ✅ Can demonstrate end-to-end testing pipeline
  ✅ Reports are professional and show good coverage
```

---

##  GITHUB ACTIONS STATUS BADGES

After setting up each repo, add to README.md:

**bank-customer-onboarding-service:**
```markdown
[![Backend CI/CD](https://github.com/YOUR_USERNAME/bank-customer-onboarding-service/workflows/Backend%20CI%2FCD/badge.svg)](https://github.com/YOUR_USERNAME/bank-customer-onboarding-service/actions)
```

**bank-onboarding-blackbox-api:**
```markdown
[![API Tests](https://github.com/YOUR_USERNAME/bank-onboarding-blackbox-api/workflows/API%20Blackbox%20Tests/badge.svg)](https://github.com/YOUR_USERNAME/bank-onboarding-blackbox-api/actions)
```

**bank-onboarding-ui-e2e:**
```markdown
[![UI Tests](https://github.com/YOUR_USERNAME/bank-onboarding-ui-e2e/workflows/UI%20E2E%20Tests/badge.svg)](https://github.com/YOUR_USERNAME/bank-onboarding-ui-e2e/actions)
```

---

##  TROUBLESHOOTING QUICK FIXES

| Problem | Diagnosis | Fix |
|---------|-----------|-----|
| API tests timeout | Backend not healthy | Run `curl http://localhost:8080/actuator/health` in Terminal 1 |
| UI tests fail with "ECONNREFUSED" | Backend not running | Check Terminal 1 output; restart docker-compose |
| Docker image pull fails | Network/auth issue | Run `docker login ghcr.io` with GitHub token |
| Reports not generated | Gradle/npm scripts issue | Check build.gradle (allure plugin) or package.json (test:report script) |
| Flaky UI tests | Timing/race conditions | Add `--retries 2` flag or increase timeouts in playwright.config.ts |
| GitHub Actions doesn't trigger | repository_dispatch not configured | Verify event type matches listener in workflow |

---

##  PORTFOLIO TALKING POINTS

### Local Demo (30 sec)

> "I have all three repos cloned locally. Let me start the backend with docker-compose, then I'll run the API tests using Rest Assured, and finally the UI tests with Playwright. You can see the reports showing comprehensive test coverage across all layers."

### CI/CD Explanation (1 min)

> "When I push code, GitHub Actions automatically runs the backend build and unit tests. Once the Docker image is ready, it triggers the API test repository, which pulls the image, starts the backend, and runs Rest Assured tests. If those pass, it triggers the UI repo, which starts the same backend and runs Playwright E2E tests. All reports are stored as artifacts for audit trails."

### Portfolio Value (2 min)

> "This three-repo setup demonstrates:
> - Independent test repositories (real-world CI/CD practice)
> - Multi-repo orchestration via `repository_dispatch` (GitHub Actions expertise)
> - Type-safe testing at all layers (Rest Assured + Playwright)
> - Professional reporting (Allure + Playwright HTML)
> - Container-based testing (Docker Desktop integration)
> - Portfolio-grade documentation and README files
>
> This is how teams at companies like NAB or Atlassian structure test automation at scale."

---

##  QUICK REFERENCE CHEAT SHEET

```
┌─────────────────────────────────────────────────────────┐
│ CLONE ALL REPOS                                         │
└─────────────────────────────────────────────────────────┘
cd ~/portfolio && git clone [3 repos]

┌─────────────────────────────────────────────────────────┐
│ LOCAL: SMOKE TEST (15 min)                              │
└─────────────────────────────────────────────────────────┘
T1: cd bank-customer-onboarding-service && docker-compose up
T2: cd bank-onboarding-blackbox-api && ./gradlew smokeTest
T3: cd bank-onboarding-ui-e2e && npm run test:smoke

┌─────────────────────────────────────────────────────────┐
│ LOCAL: FULL REGRESSION (50 min)                         │
└─────────────────────────────────────────────────────────┘
T1: cd bank-customer-onboarding-service && docker-compose up
T2: cd bank-onboarding-blackbox-api && ./gradlew blackboxTest
T3: cd bank-onboarding-ui-e2e && npm run test:e2e

┌─────────────────────────────────────────────────────────┐
│ VIEW REPORTS                                            │
└─────────────────────────────────────────────────────────┘
API:  open build/reports/allure/index.html
UI:   npx playwright show-report

┌─────────────────────────────────────────────────────────┐
│ GITHUB ACTIONS: AUTO-TRIGGERED                          │
└─────────────────────────────────────────────────────────┘
1. Push code to main
2. Backend workflow runs → builds image
3. API tests auto-triggered → runs smoke+regression
4. UI tests auto-triggered → runs smoke+regression
5. All artifacts available in Actions tab

┌─────────────────────────────────────────────────────────┐
│ PORTFOLIO DEMO: Show                                    │
└─────────────────────────────────────────────────────────┘
✅ Three repos side-by-side (GitHub)
✅ GitHub Actions "passing" badges
✅ Local test execution in terminals
✅ Allure and Playwright reports
✅ README files with clear instructions
```

---

**Now you're ready to demonstrate a production-grade, three-repo testing architecture!** 

Start with the local smoke test, then show the full regression, then open GitHub and explain the CI/CD orchestration.
