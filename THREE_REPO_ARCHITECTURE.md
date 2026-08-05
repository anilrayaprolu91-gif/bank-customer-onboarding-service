# Three-Repository Testing Architecture
## Bank Customer Onboarding Portfolio Project

**Scenario:** Separate GitHub repos for backend, API tests (Rest Assured), and UI tests (Playwright)  
**Setup:** Docker Desktop + GitHub Actions + Local development workflows  
**Portfolio Value:** Demonstrates multi-repo orchestration, CI/CD governance, test automation at scale

---

## 🏗️ THREE-REPO STRUCTURE

```
Your GitHub Account
├── bank-customer-onboarding-service/          (Backend + unit/component tests)
│   ├── src/main/java/com/bank/onboarding/    (Spring Boot app)
│   ├── src/test/java/                         (unit tests)
│   ├── src/componentTest/java/                (component tests)
│   ├── frontend/                              (React + TypeScript UI)
│   ├── Dockerfile                             (monolith: backend + UI)
│   ├── docker-compose.yml                     (postgres + kafka + app)
│   └── Jenkinsfile / .github/workflows/       (build + unit tests)
│
├── bank-onboarding-blackbox-api/              (Rest Assured API tests)
│   ├── src/test/java/com/bank/onboarding/    (blackbox tests)
│   ├── build.gradle                           (Rest Assured + JUnit 5)
│   ├── src/test/resources/                    (Allure configs, test data)
│   └── .github/workflows/                     (API test pipeline)
│
└── bank-onboarding-ui-e2e/                    (Playwright UI tests)
    ├── src/pages/                             (Playwright page objects)
    ├── src/tests/                             (UI test scenarios)
    ├── package.json                           (Node + Playwright deps)
    ├── playwright.config.ts                   (config)
    └── .github/workflows/                     (E2E test pipeline)
```

---

## 🚀 LOCAL DEVELOPMENT SETUP

### Step 1: Clone All Three Repos

```bash
# Create a portfolio project directory
mkdir bank-onboarding-portfolio
cd bank-onboarding-portfolio

# Clone backend repo
git clone https://github.com/YOUR_USERNAME/bank-customer-onboarding-service.git
cd bank-customer-onboarding-service

# Clone API test repo (sibling)
cd ..
git clone https://github.com/YOUR_USERNAME/bank-onboarding-blackbox-api.git

# Clone UI test repo (sibling)
git clone https://github.com/YOUR_USERNAME/bank-onboarding-ui-e2e.git

# Now you have:
# bank-onboarding-portfolio/
# ├── bank-customer-onboarding-service/
# ├── bank-onboarding-blackbox-api/
# └── bank-onboarding-ui-e2e/
```

### Step 2: Start Backend (Docker Desktop)

**Terminal 1 — Start backend + database:**

```bash
cd bank-customer-onboarding-service

# Ensure Docker Desktop is running
docker ps  # Should work without errors

# Start all services
docker-compose up --build

# Expected output:
# postgres_1       | ... ready to accept connections
# kafka_1          | ... started
# app_1            | ... Tomcat started on port 8080

# Verify backend is healthy
curl http://localhost:8080/actuator/health
# Response: {"status":"UP"}
```

**Verify services:**
- Backend: `http://localhost:8080/swagger-ui.html`
- Postgres: localhost:5432
- Kafka: localhost:9092

### Step 3: Run REST Assured Tests Locally

**Terminal 2 — Run API blackbox tests:**

```bash
cd bank-onboarding-blackbox-api

# Install dependencies
./gradlew build --no-daemon

# Run all blackbox tests against running backend
./gradlew blackboxTest --no-daemon \
  -Dapi.profile=local \
  -Dapi.baseUrl=http://localhost:8080

# Run only smoke tests (faster feedback)
./gradlew blackboxTest --no-daemon \
  -Dapi.profile=local \
  -Dapi.baseUrl=http://localhost:8080 \
  --tests "*SmokeTest"

# View Allure report
open build/reports/allure/index.html
```

### Step 4: Run Playwright UI Tests Locally

**Terminal 3 — Run UI E2E tests:**

```bash
cd bank-onboarding-ui-e2e

# Install dependencies
npm ci

# Run Playwright tests against backend (need frontend running)
# Option A: If frontend is built into backend
npm run test:e2e -- --baseURL http://localhost:8080

# Option B: Run frontend dev server (from backend repo)
cd ../bank-customer-onboarding-service/frontend
npm run dev  # runs on http://localhost:5173 with /api proxy

# Then run tests against dev frontend
cd ../../bank-onboarding-ui-e2e
npm run test:e2e -- --baseURL http://localhost:5173

# View Playwright report
npx playwright show-report
```

### Step 5: Local Test Execution Summary

```
TERMINAL 1: docker-compose up          → Backend + Postgres + Kafka
TERMINAL 2: gradle blackboxTest         → REST Assured API tests (10-15 min)
TERMINAL 3: npm run test:e2e            → Playwright UI tests (5-10 min)

✅ All tests running against LOCAL backend
✅ Independent execution (no coupling between repos)
```

---

## 🐳 DOCKER DESKTOP LOCAL WORKFLOW

### When Running Everything Locally

```bash
# Root of portfolio project
bank-onboarding-portfolio/

# Step 1: Terminal 1 — Start backend
cd bank-customer-onboarding-service
docker-compose up --build

# Step 2: Terminal 2 — Run API tests
cd ../bank-onboarding-blackbox-api
./gradlew blackboxTest \
  -Dapi.profile=local \
  -Dapi.baseUrl=http://localhost:8080 \
  -Dapi.logLevel=INFO

# Step 3: Terminal 3 — Run UI tests
cd ../bank-onboarding-ui-e2e
npm run test:e2e -- --baseURL http://localhost:8080

# Step 4: View reports
# API: bank-onboarding-blackbox-api/build/reports/allure
# UI: bank-onboarding-ui-e2e/playwright-report
```

### Key Environment Variables

Each test repo needs to know where the backend is:

**bank-onboarding-blackbox-api/build.gradle:**
```groovy
test {
    systemProperties = [
        'api.baseUrl': System.getProperty('api.baseUrl', 'http://localhost:8080'),
        'api.profile': System.getProperty('api.profile', 'local'),
        'api.logLevel': System.getProperty('api.logLevel', 'INFO'),
    ]
}
```

**bank-onboarding-ui-e2e/playwright.config.ts:**
```typescript
export default defineConfig({
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:8080',
    trace: 'on-first-retry',
  },
})
```

---

## 🔄 GITHUB ACTIONS MULTI-REPO PIPELINES

### Architecture

```
main branch push
    ↓
[Backend Repo]
├─ Build Spring Boot app
├─ Run unit tests
├─ Run component tests
├─ Build Docker image
└─ Push to registry
    ↓
[API Test Repo] (triggered by backend)
├─ Pull backend image
├─ Start backend (docker-compose)
├─ Run REST Assured blackbox tests
├─ Generate Allure report
└─ Upload artifacts
    ↓
[UI Test Repo] (after API tests pass)
├─ Pull backend image
├─ Start backend + frontend
├─ Run Playwright E2E tests
├─ Generate HTML report
└─ Upload artifacts
    ↓
Status: ✅ All tests passed or ❌ Failed at X layer
```

### 1. Backend Repository Workflow

**`.github/workflows/backend-ci.yml`** (in bank-customer-onboarding-service)

```yaml
name: Backend CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}/backend

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    outputs:
      image-tag: ${{ steps.image.outputs.tag }}
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Run unit tests
        run: ./gradlew test --no-daemon
      
      - name: Run component tests
        run: ./gradlew componentTest --no-daemon
      
      - name: Build application
        run: ./gradlew build -x test --no-daemon
      
      - name: Run SonarQube analysis (optional)
        run: ./gradlew sonarqube --no-daemon
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        if: github.event_name == 'push'
      
      - name: Build Docker image
        id: image
        run: |
          docker build -t ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }} .
          docker build -t ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest .
          echo "tag=${{ github.sha }}" >> $GITHUB_OUTPUT
      
      - name: Login to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Push Docker image
        run: |
          docker push ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
          docker push ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest

  trigger-api-tests:
    needs: build-and-test
    runs-on: ubuntu-latest
    if: success()
    
    steps:
      - name: Trigger API test repository
        uses: actions/github-script@v7
        with:
          github-token: ${{ secrets.GITHUB_TOKEN }}
          script: |
            await github.rest.repos.createDispatchEvent({
              owner: context.repo.owner,
              repo: 'bank-onboarding-blackbox-api',
              event_type: 'backend-ready',
              client_payload: {
                backend_image: '${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ needs.build-and-test.outputs.image-tag }}',
                backend_version: '${{ github.sha }}',
                trigger_branch: context.ref.replace('refs/heads/', ''),
              }
            })

  publish-reports:
    needs: build-and-test
    runs-on: ubuntu-latest
    if: always()
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Upload test reports
        uses: actions/upload-artifact@v4
        with:
          name: backend-test-reports
          path: build/reports/tests/
      
      - name: Upload Allure results
        uses: actions/upload-artifact@v4
        with:
          name: backend-allure-results
          path: build/allure-results/
```

### 2. API Test Repository Workflow

**`.github/workflows/api-tests.yml`** (in bank-onboarding-blackbox-api)

```yaml
name: API Blackbox Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:
    inputs:
      backend_image:
        description: 'Backend Docker image to test against'
        required: false
  repository_dispatch:
    types: [ backend-ready ]

env:
  REGISTRY: ghcr.io
  BACKEND_REPO: ${{ github.repository_owner }}/bank-customer-onboarding-service

jobs:
  run-blackbox-tests:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: onboarding
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Set backend image tag
        id: backend-image
        run: |
          if [ "${{ github.event_name }}" == "repository_dispatch" ]; then
            echo "image=${{ github.event.client_payload.backend_image }}" >> $GITHUB_OUTPUT
          else
            echo "image=${{ env.REGISTRY }}/${{ env.BACKEND_REPO }}/backend:latest" >> $GITHUB_OUTPUT
          fi
      
      - name: Login to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Pull backend image
        run: docker pull ${{ steps.backend-image.outputs.image }}
      
      - name: Start backend service
        run: |
          docker run \
            --name backend-service \
            --network host \
            -e DATABASE_URL=jdbc:postgresql://localhost:5432/onboarding \
            -e DATABASE_USERNAME=postgres \
            -e DATABASE_PASSWORD=postgres \
            -e SPRING_PROFILES_ACTIVE=embedded \
            -d ${{ steps.backend-image.outputs.image }}
      
      - name: Wait for backend to be ready
        run: |
          for i in {1..30}; do
            if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
              echo "Backend is healthy"
              exit 0
            fi
            echo "Waiting for backend... ($i/30)"
            sleep 2
          done
          echo "Backend failed to start"
          exit 1
      
      - name: Run smoke tests
        run: ./gradlew blackboxTest \
          -Dapi.profile=local \
          -Dapi.baseUrl=http://localhost:8080 \
          --tests "*SmokeTest" \
          --no-daemon
      
      - name: Run full regression tests
        if: github.event_name == 'push' && github.ref == 'refs/heads/main'
        run: ./gradlew blackboxTest \
          -Dapi.profile=local \
          -Dapi.baseUrl=http://localhost:8080 \
          --no-daemon
      
      - name: Generate Allure report
        if: always()
        run: |
          ./gradlew allureReport --no-daemon || true
      
      - name: Upload Allure results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: api-allure-results
          path: build/allure-results/
      
      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: api-test-reports
          path: build/reports/tests/blackboxTest/
      
      - name: Trigger UI tests
        if: success()
        uses: actions/github-script@v7
        with:
          github-token: ${{ secrets.GITHUB_TOKEN }}
          script: |
            await github.rest.repos.createDispatchEvent({
              owner: context.repo.owner,
              repo: 'bank-onboarding-ui-e2e',
              event_type: 'api-tests-passed',
              client_payload: {
                backend_image: '${{ steps.backend-image.outputs.image }}',
                api_tests_status: 'passed',
                triggered_by: 'api-test-workflow',
              }
            })
      
      - name: Comment on PR
        if: github.event_name == 'pull_request'
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: '✅ API Blackbox tests passed!'
            })
```

### 3. UI Test Repository Workflow

**`.github/workflows/ui-tests.yml`** (in bank-onboarding-ui-e2e)

```yaml
name: UI E2E Tests (Playwright)

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:
  repository_dispatch:
    types: [ api-tests-passed ]

env:
  REGISTRY: ghcr.io
  BACKEND_REPO: ${{ github.repository_owner }}/bank-customer-onboarding-service

jobs:
  run-e2e-tests:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: onboarding
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Install Playwright browsers
        run: npm run install-browsers
      
      - name: Set backend image tag
        id: backend-image
        run: |
          if [ "${{ github.event_name }}" == "repository_dispatch" ]; then
            echo "image=${{ github.event.client_payload.backend_image }}" >> $GITHUB_OUTPUT
          else
            echo "image=${{ env.REGISTRY }}/${{ env.BACKEND_REPO }}/backend:latest" >> $GITHUB_OUTPUT
          fi
      
      - name: Login to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Pull backend image
        run: docker pull ${{ steps.backend-image.outputs.image }}
      
      - name: Start backend service
        run: |
          docker run \
            --name backend-service \
            --network host \
            -e DATABASE_URL=jdbc:postgresql://localhost:5432/onboarding \
            -e DATABASE_USERNAME=postgres \
            -e DATABASE_PASSWORD=postgres \
            -e SPRING_PROFILES_ACTIVE=embedded \
            -d ${{ steps.backend-image.outputs.image }}
      
      - name: Wait for backend to be ready
        run: |
          for i in {1..30}; do
            if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
              echo "Backend is healthy"
              exit 0
            fi
            echo "Waiting for backend... ($i/30)"
            sleep 2
          done
          echo "Backend failed to start"
          exit 1
      
      - name: Run smoke tests
        run: npm run test:smoke -- --baseURL http://localhost:8080
      
      - name: Run full regression tests
        if: github.event_name == 'push' && github.ref == 'refs/heads/main'
        run: npm run test:e2e -- --baseURL http://localhost:8080
      
      - name: Generate HTML report
        if: always()
        run: npm run test:report || true
      
      - name: Upload Playwright report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ui-playwright-report
          path: playwright-report/
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ui-test-results
          path: test-results/
      
      - name: Comment on PR
        if: github.event_name == 'pull_request' && success()
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: '✅ UI E2E tests passed!'
            })
      
      - name: Comment on PR (failure)
        if: github.event_name == 'pull_request' && failure()
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: '❌ UI E2E tests failed! Check the artifacts for details.'
            })
```

---

## 🎯 EXECUTION SCENARIOS

### Scenario 1: LOCAL DEVELOPMENT (All Three Repos on Your Machine)

```bash
# Setup
mkdir ~/portfolio-project && cd ~/portfolio-project
git clone https://github.com/YOUR_USERNAME/bank-customer-onboarding-service.git
git clone https://github.com/YOUR_USERNAME/bank-onboarding-blackbox-api.git
git clone https://github.com/YOUR_USERNAME/bank-onboarding-ui-e2e.git

# Terminal 1: Backend
cd bank-customer-onboarding-service
docker-compose up --build
# ✅ Backend ready on http://localhost:8080

# Terminal 2: API Tests
cd ../bank-onboarding-blackbox-api
./gradlew blackboxTest -Dapi.baseUrl=http://localhost:8080 --no-daemon
# ✅ API tests run against local backend
# 📊 Report: build/reports/allure/

# Terminal 3: UI Tests
cd ../bank-onboarding-ui-e2e
npm ci
npm run test:e2e -- --baseURL http://localhost:8080
# ✅ UI tests run against local backend
# 📊 Report: playwright-report/

# Result: All tests pass locally ✅
```

### Scenario 2: GITHUB ACTIONS PIPELINE (Automated on Push)

```
Developer pushes to main branch
    ↓
GitHub Actions triggers:
    ├─ Backend Repo Workflow
    │   ├─ Run unit tests ✅
    │   ├─ Run component tests ✅
    │   ├─ Build Docker image
    │   └─ Push to registry
    │       ↓ [trigger = repository_dispatch]
    │
    ├─ API Test Repository Workflow (auto-triggered)
    │   ├─ Pull backend image
    │   ├─ Start backend (Docker)
    │   ├─ Wait for health check
    │   ├─ Run smoke tests ✅
    │   ├─ Run regression (if main branch) ✅
    │   ├─ Generate Allure report
    │   ├─ Upload artifacts
    │   └─ [trigger = repository_dispatch]
    │       ↓
    │
    └─ UI Test Repository Workflow (auto-triggered)
        ├─ Pull backend image
        ├─ Start backend (Docker)
        ├─ Wait for health check
        ├─ Run smoke tests ✅
        ├─ Run regression (if main branch) ✅
        ├─ Generate Playwright report
        └─ Upload artifacts

Final Status: ✅ All tests passed (reported in GitHub checks)
```

### Scenario 3: PULL REQUEST TESTING

When you open a PR to main:

```
PR opened against main
    ↓
GitHub Actions runs:
    ├─ Backend: unit + component tests only
    ├─ API: smoke tests only (faster feedback)
    └─ UI: smoke tests only (faster feedback)

If all pass: ✅ "All checks passed" badge shows in PR
If any fails: ❌ "X checks failed" badge + detailed logs
```

---

## 📊 PORTFOLIO PRESENTATION SCRIPT

### "Walk me through your test automation setup"

> "I've architected a three-repository testing strategy for this banking onboarding project:
>
> **Repository 1: Backend** (`bank-customer-onboarding-service`)
> - Spring Boot REST API with unit and component tests
> - Containerized with Docker Compose (Postgres + Kafka)
> - Deployed to GitHub Container Registry on every merge to main
>
> **Repository 2: API Blackbox Tests** (`bank-onboarding-blackbox-api`)
> - Rest Assured framework with ~30 test cases
> - Covers smoke, regression, contract validation, performance baselines
> - Automatically triggered after backend build succeeds
> - Generates Allure reports for stakeholder visibility
>
> **Repository 3: UI E2E Tests** (`bank-onboarding-ui-e2e`)
> - Playwright framework with ~25 UI test cases
> - Covers navigation, form validation, error handling, accessibility
> - Triggered after API tests pass
> - Generates HTML reports with screenshots/traces
>
> **How it runs:**
> - **Locally:** I clone all three repos, start the backend with docker-compose, then run tests in parallel
> - **In CI/CD:** GitHub Actions orchestrates everything. Backend repo pushes image → triggers API tests → triggers UI tests
> - **On PR:** Fast-track smoke tests only for quick feedback
>
> **Key architectural decisions:**
> - Each repo is independent (can be released separately)
> - Tests run sequentially in CI (blocking gates), but repositories stay loosely coupled
> - Using repository_dispatch to coordinate without implementing a monolithic CI config
> - Reports stored as artifacts for audit trail
>
> This reflects real-world practices at banks where different teams own different components."

---

## 🛠️ SETUP CHECKLIST

### GitHub Repository Configuration

For each repo, ensure:

- [ ] Public (for portfolio visibility)
- [ ] README with "How to run" section
- [ ] GitHub Actions workflows enabled
- [ ] Branch protection `main` (require PR + status checks)
- [ ] Secrets configured (if needed):
  - `GITHUB_TOKEN` (auto-available)
  - `SONAR_TOKEN` (if using SonarQube)

### Docker Desktop

- [ ] Docker Desktop running
- [ ] `docker ps` works without errors
- [ ] Docker images can be pulled from ghcr.io

### Local Environment

Each repo needs a README:

**bank-onboarding-blackbox-api/README.md:**
```markdown
# Bank Onboarding Blackbox API Tests

## Quick Start

```bash
# Start backend
cd ../bank-customer-onboarding-service
docker-compose up --build

# Run tests
cd ../bank-onboarding-blackbox-api
./gradlew blackboxTest -Dapi.baseUrl=http://localhost:8080 --no-daemon

# View report
open build/reports/allure/index.html
```

## CI/CD
Automatically triggered by backend repo after image is pushed.
```

**bank-onboarding-ui-e2e/README.md:**
```markdown
# Bank Onboarding UI E2E Tests (Playwright)

## Quick Start

```bash
# Start backend
cd ../bank-customer-onboarding-service
docker-compose up --build

# Run tests
cd ../bank-onboarding-ui-e2e
npm ci
npm run test:e2e -- --baseURL http://localhost:8080

# View report
npx playwright show-report
```

## CI/CD
Automatically triggered after API tests pass.
```

---

## 🎁 BONUS: Portfolio Talking Points

### "How do you handle test data consistency across three repos?"

> "Each test repo has deterministic test data factories. API tests create known customer records with specific fields. UI tests can then search for those same records. Since they run sequentially in CI, the database state is preserved between layers."

### "What if API tests fail — do UI tests still run?"

> "No — the API test workflow has a final step that only triggers UI tests if API tests pass. This prevents wasting CI minutes on UI tests when the API is broken. On PR, we run smoke tests from both layers in parallel for faster feedback, but on main branch (preparing for release), we run full suites sequentially with blocking gates."

### "How do you demonstrate this to a recruiter?"

> "I show them:
> 1. Open the three repos side-by-side
> 2. Click 'Actions' tab → show green check marks and workflow runs
> 3. Show a PR with all three test suites running as status checks
> 4. Show locally cloned repos running tests in terminal
> 5. Open Allure report + Playwright HTML report as examples of professional reporting"

---

## 🔗 QUICK REFERENCE COMMANDS

### Local Setup (First Time)
```bash
mkdir bank-onboarding-portfolio && cd bank-onboarding-portfolio
git clone https://github.com/YOUR_USERNAME/bank-customer-onboarding-service.git
git clone https://github.com/YOUR_USERNAME/bank-onboarding-blackbox-api.git
git clone https://github.com/YOUR_USERNAME/bank-onboarding-ui-e2e.git
```

### Run All Tests Locally
```bash
# Terminal 1
cd bank-customer-onboarding-service && docker-compose up --build

# Terminal 2
cd bank-onboarding-blackbox-api && ./gradlew blackboxTest -Dapi.baseUrl=http://localhost:8080 --no-daemon

# Terminal 3
cd bank-onboarding-ui-e2e && npm ci && npm run test:e2e -- --baseURL http://localhost:8080
```

### View Reports
```bash
# API Allure report
open bank-onboarding-blackbox-api/build/reports/allure/index.html

# UI Playwright report
cd bank-onboarding-ui-e2e && npx playwright show-report
```

### Simulate CI Locally (Docker)
```bash
cd bank-customer-onboarding-service
docker-compose up --build-arg SKIP_FRONTEND=true

# In another terminal, run API tests
cd ../bank-onboarding-blackbox-api
./gradlew blackboxTest -Dapi.profile=container -Dapi.baseUrl=http://localhost:8080
```

---

## 📋 TROUBLESHOOTING

| Problem | Solution |
|---------|----------|
| API tests fail with `Connection refused` | Backend not running; run `docker-compose up` first |
| UI tests timeout waiting for elements | Frontend not built; ensure backend includes React UI |
| GitHub Actions workflow not triggering | Check `repository_dispatch` event type is in listener; verify `GITHUB_TOKEN` permissions |
| Docker image pull fails in CI | Verify Container Registry login step runs before pull |
| Allure report blank | Check `build.gradle` has allure plugin; run `./gradlew allureReport` |

---

This three-repo setup is **production-grade**, **portfolio-friendly**, and **team-scalable**. You can demonstrate it to recruiters showing independent test execution, CI/CD orchestration, and professional reporting.


