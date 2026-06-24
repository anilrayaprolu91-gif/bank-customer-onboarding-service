# Contract Testing — Enterprise Demo Guide

This project implements a **two-track contract testing strategy** that matches real enterprise patterns:

| Track | Tool | Scope |
|---|---|---|
| Provider / server-side | Spring Cloud Contract | Verifies this service honours the API contracts it publishes |
| Consumer / client-side | Pact JUnit 5 | Verifies consumers (e.g. portal, mobile) agree on what they need from this service |

---

## Architecture overview

```
┌─────────────────────────────────────────────────────────────────────┐
│  src/test/resources/contracts/             ← provider contract DSL  │
│  src/test/java/.../BaseContractTest.java   ← MockMvc base class     │
│  src/consumerContractTest/...PactTest.java ← consumer pact test     │
│  build/pacts/*.json                        ← generated pact files   │
│  build/stubs/*.jar                         ← publishable stub jar   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Test layers at a glance

```
contractTest              — Spring Cloud Contract verifier (provider side)
consumerContractTest      — Pact consumer contract (consumer side)
publishContractStubs      — build + publish stub JAR to local Maven cache
publishContractStubsToRepository  — same, targeting a remote Nexus/Artifactory
```

---

## How to run locally (no Docker needed)

### 1 — Unit tests (fast, no Spring context)

```powershell
Set-Location "D:\bank-customer-onboarding-service"
.\gradlew.bat test --no-daemon
```

### 2 — Provider contract tests (Spring Cloud Contract)

```powershell
.\gradlew.bat generateContractTests --no-daemon   # optional: preview generated verifier
.\gradlew.bat contractTest --no-daemon
```

Reports: `build/reports/tests/contractTest/index.html`

### 3 — Consumer contract tests (Pact)

```powershell
.\gradlew.bat consumerContractTest --no-daemon
```

Generated Pact files: `build/pacts/digital-onboarding-portal-bank-customer-onboarding-provider.json`

Reports: `build/reports/tests/consumerContractTest/index.html`

### 4 — Publish stubs to local Maven cache

```powershell
.\gradlew.bat publishContractStubs --no-daemon
```

Artifact stored at:
```
~/.m2/repository/com/bank/bank-customer-onboarding-service-stubs/1.0.0-SNAPSHOT/
```

### 5 — Publish stubs to remote Nexus / Artifactory

```powershell
$env:STUBS_REPO_URL      = "https://nexus.bank.internal/repository/contract-stubs"
$env:STUBS_REPO_USERNAME = "svc_contract_publisher"
$env:STUBS_REPO_PASSWORD = "<your-token>"

.\gradlew.bat publishContractStubsToRepository --no-daemon `
    -DSTUBS_REPO_URL=$env:STUBS_REPO_URL `
    -DSTUBS_REPO_USERNAME=$env:STUBS_REPO_USERNAME `
    -DSTUBS_REPO_PASSWORD=$env:STUBS_REPO_PASSWORD
```

> **Note:** `publishContractStubsToRepository` is only registered when `STUBS_REPO_URL` is set
> (either as an environment variable or a `-D` system property). This prevents accidental remote
> pushes from developer machines without configuration.

### 6 — Run all contract layers in one shot

```powershell
.\gradlew.bat consumerContractTest contractTest publishContractStubs --no-daemon
```

---

## How to run via Docker Desktop

Docker Desktop is required for Testcontainers-backed tests (component, Kafka, blackbox-with-Kafka).
Contract tests themselves do **not** need Docker — they use MockMvc / Pact mock servers.

### Prerequisites

1. Install [Docker Desktop](https://www.docker.com/products/docker-desktop/) ≥ 4.x
2. Ensure Docker is running: `docker info`
3. Set the Docker host for Windows named pipe (Testcontainers default):

```powershell
$env:DOCKER_HOST = "npipe:////./pipe/docker_engine"
```

### Run the full test pyramid via Docker Desktop

```powershell
Set-Location "D:\bank-customer-onboarding-service"

# 1. Unit tests
.\gradlew.bat test --no-daemon

# 2. Provider contract tests
.\gradlew.bat contractTest --no-daemon

# 3. Consumer contract tests (Pact)
.\gradlew.bat consumerContractTest --no-daemon

# 4. Component tests (Testcontainers — needs Docker)
.\gradlew.bat componentTest --no-daemon

# 5. Blackbox API tests (embedded Spring + optional Kafka Testcontainer)
.\gradlew.bat blackboxTest --no-daemon

# 6. Full check (all of the above in one command)
.\gradlew.bat check --no-daemon
```

### Run the full application stack with Kafka via docker-compose

```powershell
Set-Location "D:\bank-customer-onboarding-service"
docker-compose up --build
```

Services started:
- `postgres` on port 5432
- `kafka` (Redpanda) on port 9092
- `app` (onboarding service) on port 8080

Verify it's healthy:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
```

### Run the specific Kafka Testcontainers tests

```powershell
# Positive flow: event produced and consumed
.\gradlew.bat blackboxTest --tests "*KafkaOnboardingBlackboxApiTest" --no-daemon

# Negative flow: graceful degradation when Kafka is disabled
.\gradlew.bat blackboxTest --tests "*KafkaDisabledOnboardingBlackboxApiTest" --no-daemon

# Component-layer Kafka test
.\gradlew.bat componentTest --tests "*KafkaCustomerOnboardingComponentTest" --no-daemon
```

---

## CI pipeline stages (Jenkinsfile)

The `Jenkinsfile` wires contract testing into the quality gate chain:

```
Checkout
  └─ Build
       └─ Test Execution (parallel)
            ├─ Unit Test
            ├─ Component Test
            └─ Blackbox Test
                 └─ Contract Test          ← Spring Cloud Contract (blocks SonarQube/SpotBugs)
                      └─ Consumer Contract Test   ← Pact consumer contracts
                           └─ Publish Contract Stubs  ← main / release/* ONLY, with credentials
                                └─ SonarQube → SpotBugs → Jacoco → Docker Build → ...
```

### Stub publishing stage detail

The `Publish Contract Stubs` stage:
- Only runs on `main` or `release/**` branches (never on PRs or feature branches)
- Requires `CONTRACT_TEST_PASSED = true` (won't publish broken stubs)
- Reads credentials from the Jenkins credential store via `withCredentials`
- Falls back to local Maven cache publish when `STUBS_REPO_URL` is not configured

### Jenkins parameters controlling contract testing

| Parameter | Default | Purpose |
|---|---|---|
| `RUN_CONTRACT_TESTS` | `true` | Toggle Spring Cloud Contract verifier |
| `RUN_CONSUMER_CONTRACT_TESTS` | `true` | Toggle Pact consumer contract stage |
| `PUBLISH_CONTRACT_STUBS` | `true` | Toggle stub artifact publishing |
| `STUBS_CREDENTIALS_ID` | `nexus-contract-stubs-creds` | Jenkins credential ID for the stubs repo |
| `STUBS_REPO_URL` | _(empty)_ | Remote Maven repo URL; falls back to local Maven when blank |

### Create the Jenkins credential (one-time setup)

In Jenkins → **Manage Credentials → Add Credentials**:
- Kind: **Username with password**
- ID: `nexus-contract-stubs-creds`
- Username: service account (e.g. `svc_contract_publisher`)
- Password: Nexus/Artifactory API token

---

## Enterprise talking points for demo

| Pattern | What it shows |
|---|---|
| **Provider contracts** (Spring Cloud Contract) | The API is specified as executable code, not just documentation. Any change that breaks a consumer's expectation fails the build immediately. |
| **Consumer contracts** (Pact) | Consumers drive the API design from their perspective. Contracts live as versioned JSON files that can be shared via a Pact Broker. |
| **Stub JAR publishing** | Downstream teams consume the stub JAR in their own tests — no shared environment needed, no coordination overhead. |
| **Branch-gated publishing** | Stubs are only published from trusted branches, preventing half-baked contracts from leaking to consumers. |
| **Credential injection** | No secrets in code or Gradle files — Jenkins `withCredentials` block injects at runtime. |
| **Quality gate chain** | Contract tests must pass before SonarQube, SpotBugs, or Docker push can proceed. |
