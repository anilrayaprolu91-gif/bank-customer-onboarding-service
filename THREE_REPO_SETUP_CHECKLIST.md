# Three-Repo Setup — Step-by-Step Implementation

This guide walks you through creating and configuring three separate repositories with integrated test pipelines.

---

##  PHASE 1: REPOSITORY SETUP (30 Minutes)

### Step 1: Create Three GitHub Repositories

In GitHub, create three public repositories:

1. **bank-customer-onboarding-service** (exists, update if needed)
2. **bank-onboarding-blackbox-api** (NEW)
3. **bank-onboarding-ui-e2e** (NEW)

### Step 2: Clone Locally

```bash
mkdir ~/bank-onboarding-portfolio
cd ~/bank-onboarding-portfolio

# Clone or create the three repos as siblings
git clone https://github.com/YOUR_USERNAME/bank-customer-onboarding-service.git
git clone https://github.com/YOUR_USERNAME/bank-onboarding-blackbox-api.git
git clone https://github.com/YOUR_USERNAME/bank-onboarding-ui-e2e.git

# Verify structure
tree -L 1 ~/bank-onboarding-portfolio
# Output:
# bank-onboarding-portfolio/
# ├── bank-customer-onboarding-service/
# ├── bank-onboarding-blackbox-api/
# └── bank-onboarding-ui-e2e/
```

---

##  PHASE 2: REPO 1 — BACKEND (UPDATE)

Your backend repo already exists. Update it with:

### 2.1 Add GitHub Actions Workflow

Create file: **`.github/workflows/backend-ci.yml`**

```yaml
name: Backend CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  workflow_dispatch:

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
      
      - name: Setup Gradle
        uses: gradle/gradle-build-action@v3
      
      - name: Run unit tests
        run: ./gradlew test --no-daemon
      
      - name: Run component tests
        run: ./gradlew componentTest --no-daemon
      
      - name: Build application
        run: ./gradlew build -x test --no-daemon
      
      - name: Build Docker image
        id: image
        run: |
          TAG="${{ github.sha }}"
          docker build -t ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:$TAG .
          docker build -t ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest .
          echo "tag=$TAG" >> $GITHUB_OUTPUT
      
      - name: Login to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Push Docker image
        run: |
          docker push ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ steps.image.outputs.tag }}
          docker push ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
      
      - name: Trigger API test repository
        if: success()
        uses: actions/github-script@v7
        with:
          github-token: ${{ secrets.GITHUB_TOKEN }}
          script: |
            await github.rest.repos.createDispatchEvent({
              owner: context.repo.owner,
              repo: 'bank-onboarding-blackbox-api',
              event_type: 'backend-ready',
              client_payload: {
                backend_image: '${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ steps.image.outputs.tag }}',
                backend_version: '${{ github.sha }}',
              }
            })
      
      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: backend-test-reports
          path: build/reports/tests/
          retention-days: 7
```

### 2.2 Add README Section

Add to **`README.md`**:

```markdown
## Running Tests

### Local Backend + Tests

```bash
# Terminal 1: Start backend
docker-compose up --build

# Terminal 2: Run API tests
cd ../bank-onboarding-blackbox-api
./gradlew blackboxTest -Dapi.baseUrl=http://localhost:8080 --no-daemon

# Terminal 3: Run UI tests
cd ../bank-onboarding-ui-e2e
npm run test:e2e -- --baseURL http://localhost:8080
```

### CI/CD Pipeline

- Backend tests run on every push
- Docker image pushed to GitHub Container Registry
- API and UI tests automatically triggered via `repository_dispatch`
```

---

##  PHASE 3: REPO 2 — REST ASSURED BLACKBOX TESTS (NEW)

### 3.1 Initialize New Repository

```bash
cd ~/bank-onboarding-portfolio/bank-onboarding-blackbox-api

# Initialize Git if new repo
git init

# Create folder structure
mkdir -p src/test/java/com/bank/onboarding/api/{tests,builders,client,config,filters,support}
mkdir -p src/test/resources/{schemas,testdata}
mkdir -p .github/workflows
```

### 3.2 Add build.gradle

Create **`build.gradle`**:

```groovy
plugins {
    id 'java'
    id 'io.qameta.allure' version '2.11.0'
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'io.rest-assured:rest-assured:5.4.1'
    testImplementation 'io.rest-assured:json-schema-validator:5.4.1'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.1'
    testImplementation 'com.fasterxml.jackson.core:jackson-databind:2.16.1'
    testImplementation 'org.assertj:assertj-core:3.24.1'
    testImplementation 'io.qameta.allure:allure-junit5:2.25.0'
    testImplementation 'org.awaitility:awaitility:4.14.1'
}

test {
    useJUnitPlatform()
    systemProperties = [
        'api.baseUrl': System.getProperty('api.baseUrl', 'http://localhost:8080'),
        'api.profile': System.getProperty('api.profile', 'local'),
    ]
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }
}

tasks.register('blackboxTest', Test) {
    useJUnitPlatform {
        includeTags 'blackbox'
    }
    systemProperties = [
        'api.baseUrl': System.getProperty('api.baseUrl', 'http://localhost:8080'),
        'api.profile': System.getProperty('api.profile', 'local'),
    ]
}

tasks.register('smokeTest', Test) {
    useJUnitPlatform {
        includeTags 'smoke'
    }
}

tasks.register('regressionTest', Test) {
    useJUnitPlatform {
        includeTags 'regression'
    }
}

allure {
    version = '2.25.0'
    autoconfigure = true
    aspectjWeaver = true
    resultsDir = file('build/allure-results')
}

tasks.register('allureReport') {
    description = 'Generate Allure report'
    dependsOn blac

Test
}
```

### 3.3 Add GitHub Actions Workflow

Create **`.github/workflows/api-tests.yml`**:

```yaml
name: API Blackbox Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  workflow_dispatch:
  repository_dispatch:
    types: [backend-ready]

jobs:
  api-tests:
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
      
      - name: Setup Gradle
        uses: gradle/gradle-build-action@v3
      
      - name: Determine backend image
        id: backend-image
        run: |
          if [ "${{ github.event_name }}" == "repository_dispatch" ]; then
            echo "image=${{ github.event.client_payload.backend_image }}" >> $GITHUB_OUTPUT
          else
            echo "image=ghcr.io/${{ github.repository_owner }}/bank-customer-onboarding-service/backend:latest" >> $GITHUB_OUTPUT
          fi
      
      - name: Login to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
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
            -d ${{ steps.backend-image.outputs.image }}
      
      - name: Wait for backend to be ready
        run: |
          for i in {1..30}; do
            if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
              echo "✅ Backend is ready"
              exit 0
            fi
            echo "Attempt $i/30: Waiting for backend..."
            sleep 2
          done
          echo "❌ Backend failed to start"
          exit 1
      
      - name: Run smoke tests
        run: ./gradlew smokeTest --no-daemon
      
      - name: Run full tests (main branch only)
        if: github.ref == 'refs/heads/main'
        run: ./gradlew blackboxTest --no-daemon
      
      - name: Generate Allure report
        if: always()
        run: ./gradlew allureReport --no-daemon
      
      - name: Upload Allure results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: api-allure-results
          path: build/allure-results/
          retention-days: 30
      
      - name: Trigger UI tests
        if: success() && github.ref == 'refs/heads/main'
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
                api_status: 'passed',
              }
            })
```

### 3.4 Add README

Create **`README.md`**:

```markdown
# Bank Onboarding Blackbox API Tests

Rest Assured + JUnit 5 framework for comprehensive API testing.

## Quick Start (Local)

```bash
# Terminal 1: Start backend
cd ../bank-customer-onboarding-service
docker-compose up --build

# Terminal 2: Run tests
cd ../bank-onboarding-blackbox-api
./gradlew smokeTest --no-daemon

# View report
./gradlew allureReport --no-daemon
open build/reports/allure/index.html
```

## Test Suites

- `smokeTest` — fast critical-path tests (~5 min)
- `regressionTest` — full coverage (~15 min)
- `blackboxTest` — all tests (~20 min)

## CI/CD

- Auto-triggered after backend image is pushed
- Blocks UI tests if API tests fail
- Generates Allure reports as artifacts
```

---

##  PHASE 4: REPO 3 — PLAYWRIGHT UI E2E TESTS (NEW)

### 4.1 Initialize Repository

```bash
cd ~/bank-onboarding-portfolio/bank-onboarding-ui-e2e

# Initialize
git init

# Create structure
mkdir -p src/{pages,tests,fixtures,utils}
mkdir -p .github/workflows
```

### 4.2 Add package.json

Create **`package.json`**:

```json
{
  "name": "bank-onboarding-ui-e2e",
  "version": "1.0.0",
  "scripts": {
    "test:e2e": "playwright test",
    "test:smoke": "playwright test --grep @smoke",
    "test:report": "playwright show-report",
    "test:ui": "playwright test --ui",
    "test:debug": "playwright test --debug"
  },
  "devDependencies": {
    "@playwright/test": "^1.40.0"
  }
}
```

### 4.3 Add playwright.config.ts

Create **`playwright.config.ts`**:

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './src/tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
    ['list'],
  ],
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:8080',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  webServer: process.env.CI ? undefined : {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
  ],
});
```

### 4.4 Add GitHub Actions Workflow

Create **`.github/workflows/ui-tests.yml`**:

```yaml
name: UI E2E Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  workflow_dispatch:
  repository_dispatch:
    types: [api-tests-passed]

jobs:
  ui-tests:
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
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Install Playwright browsers
        run: npx playwright install --with-deps
      
      - name: Determine backend image
        id: backend-image
        run: |
          if [ "${{ github.event_name }}" == "repository_dispatch" ]; then
            echo "image=${{ github.event.client_payload.backend_image }}" >> $GITHUB_OUTPUT
          else
            echo "image=ghcr.io/${{ github.repository_owner }}/bank-customer-onboarding-service/backend:latest" >> $GITHUB_OUTPUT
          fi
      
      - name: Login to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
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
            -d ${{ steps.backend-image.outputs.image }}
      
      - name: Wait for backend to be ready
        run: |
          for i in {1..30}; do
            if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
              echo "✅ Backend is ready"
              exit 0
            fi
            echo "Attempt $i/30: Waiting for backend..."
            sleep 2
          done
          echo "❌ Backend failed to start"
          exit 1
      
      - name: Run smoke tests
        run: npm run test:smoke -- --baseURL http://localhost:8080
      
      - name: Run full tests (main branch only)
        if: github.ref == 'refs/heads/main'
        run: npm run test:e2e -- --baseURL http://localhost:8080
      
      - name: Upload Playwright report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ui-playwright-report
          path: playwright-report/
          retention-days: 30
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ui-test-results
          path: test-results/
          retention-days: 7
```

### 4.5 Add README

Create **`README.md`**:

```markdown
# Bank Onboarding UI E2E Tests

Playwright framework for comprehensive UI testing.

## Quick Start (Local)

```bash
# Terminal 1: Start backend
cd ../bank-customer-onboarding-service
docker-compose up --build

# Terminal 2: Run tests
cd ../bank-onboarding-ui-e2e
npm ci
npm run test:smoke -- --baseURL http://localhost:8080

# View report
npm run test:report
```

## Test Tags

- `@smoke` — fast happy-path tests (~5 min)
- `@regression` — full suite (~15 min)

## CI/CD

- Auto-triggered after API tests pass
- Generates Playwright HTML reports
- Screenshots/videos on failure
```

---

## ✅ LOCAL TESTING CHECKLIST

Once all three repos are set up:

```bash
# 1. Clone all three repos
[ ] bank-customer-onboarding-service/
[ ] bank-onboarding-blackbox-api/
[ ] bank-onboarding-ui-e2e/

# 2. Start backend
cd bank-customer-onboarding-service
[ ] docker-compose up --build

# 3. Run API tests
cd ../bank-onboarding-blackbox-api
[ ] ./gradlew smokeTest --no-daemon

# 4. Run UI tests
cd ../bank-onboarding-ui-e2e
[ ] npm ci && npm run test:smoke -- --baseURL http://localhost:8080

# 5. View reports
[ ] open ../bank-onboarding-blackbox-api/build/reports/allure/index.html
[ ] cd bank-onboarding-ui-e2e && npx playwright show-report
```

---

##  GITHUB ACTIONS CHECKLIST

For each repo:

- [ ] `.github/workflows/` folder created
- [ ] Workflow files added (backend-ci.yml, api-tests.yml, ui-tests.yml)
- [ ] Push to GitHub
- [ ] Check "Actions" tab → workflows show green ✅
- [ ] Verify `repository_dispatch` triggers other repos

---

**You now have a three-repo portfolio project ready to demonstrate!**

See `THREE_REPO_ARCHITECTURE.md` for complete details on local setup, CI/CD, and portfolio talking points.
