# Bank Customer Onboarding Service - Jenkins Pipeline Flow

## Visual Pipeline Architecture

```
                              START
                                │
                    ┌───────────▼────────────────┐
                    │      1. CHECKOUT           │
                    │  Clone Git Repository      │
                    │  Set Docker Tag            │
                    └───────────┬────────────────┘
                                │
                    ┌───────────▼─��──────────────┐
                    │       2. BUILD             │
                    │  gradle clean assemble     │
                    │  Compile + Process         │
                    │  Annotations               │
                    └───────────┬────────────────┘
                                │
            ┌───────────────────┼──���────────────────┐
            │                   │                   │
      ┌─────▼──────┐    ┌──────▼──────┐    ┌──────▼──────┐
      │  Unit Test │    │Component    │    │   Blackbox  │
      │  (~30s)    │    │   Test      │    │    Test     │
      │ ✅ BLOCKS  │    │  (~90s)     │    │  (~3 min)   │
      │            │    │ ✅ BLOCKS   │    │ ❌ NON-BLK  │
      └─────┬──────┘    └──────┬──────┘    └──────┬──────┘
            │                  │                   │
            │      RUN IN PARALLEL (all 3 at once) │
            │                  │                   │
            └──────────┬───────┴───────────────────┘
                       │
        ┌──────────────▼──────────────────┐
        │  ALL TESTS PASSED?              │
        │  Both Unit & Component must ✅  │
        └──────────────┬──────────────────┘
                       │
                  NO ❌│  YES ✅
                       │   │
            ┌──────────┴─┐ ┌┴───────────────��──────┐
            │            │ │                       │
            │  FAIL ❌   │ │  ┌──────────────────┐  │
            │            │ │  │   4. SONARQUBE   │  │
            │            │ │  │  Code Quality    │  │
            │            │ │  │  Analysis        │  │
            │            │ │  └────────┬─────────┘  │
            │            │ │           │            │
            │            │ │  ┌────────▼─────────┐  │
            │            │ │  │ 5. QUALITY GATE  │  │
            │            │ │  │  Wait for SQ OK  │  │
            │            │ │  └────────┬─────────┘  │
            │            │ │           │            │
            │            │ │  ┌────────▼─────────┐  │
            │            │ │  │   6. SPOTBUGS    │  │
            │            │ │  │ Static Analysis  │  │
            │            │ │  └────────┬─────────┘  │
            │            │ │           │            │
            │            │ │  ┌────────▼─────────┐  │
            │            │ │  │  7. JACOCO       │  │
            │            │ │  │ Coverage Report  │  │
            │            │ │  └────────┬─────────┘  │
            │            │ │           │            │
            │            │ │  ┌────────▼─────────┐  │
            │            │ │  │ 8. DOCKER BUILD  │  │
            │            │ │  │  (if main/*release)
            │            │ │  └────────┬─────────┘  │
            │            │ │           │            │
            │            │ │ ┌─────────▼────────┐   │
            │            │ │ │ 9. APPROVAL GATE │   │
            │            │ │ │ (if *release)    │   │
            │            │ │ │ ⏳ MANUAL APPROVE│   │
            │            │ │ └────────┬────────┘    │
            │            │ │          │             │
            │            │ │  ┌───────▼────────┐    │
            │            │ │  │ 10. DOCKER PUSH│    │
            │            │ │  │ (if approved)  │    │
            │            │ │  └───────┬────────┘    │
            │            │ │          │             │
            │            │ └──────┬───┘             │
            │            │        │                │
            └────────────┼────────┴────────────────┘
                         │
              ┌──────────▼─────────────┐
              │ 11. QUALITY SUMMARY    │
              │  (Gate Flags)          │
              └──────────┬─────────────┘
                         │
              ┌──────────▼─────────────┐
              │ 12. PUBLISH REPORTS    │
              │  Archive Artifacts     │
              │  JaCoCo, Test Results  │
              └──────────┬─────────────┘
                         │
                    ┌────▼────┐
                    │  FINISH  │
                    └──────────┘
```

---

## Stage Decision Tree

```
┌─ UNIT TEST PASSED? ────────────────────────────────┐
│                                                      │
│  NO ❌  → FAIL PIPELINE                             │
│                                                      │
│  YES ✅ → Continue to Component Test               │
└──────────────────────────────────────────────────────┘

┌─ COMPONENT TEST PASSED? ───────────────────────────┐
│                                                     │
│  NO ❌  → FAIL PIPELINE                            │
│                                                     │
│  YES ✅ → Continue to Quality Stages              │
└─────────────────────────────────────────────────────┘

┌─ RUN ON ALLOWED BRANCH? ───────────────────────────┐
│  (main, develop, release/*, PR)                    │
│                                                     │
│  NO ���  → SKIP SonarQube + SpotBugs               │
│                                                     │
│  YES ✅ → Run SonarQube, Quality Gate, SpotBugs   │
└─────────────────────────────────────────────────────┘

┌─ DOCKER BUILD ALLOWED? ────────────────────────────┐
│  (main, release/*, or tag)                         │
│                                                     │
│  NO ❌  → SKIP Docker Build + Push                │
│                                                     │
│  YES ✅ → Build Docker Image                      │
└─────────────────────────────────────────────────────┘

┌─ RELEASE APPROVAL NEEDED? ─────────────────────────┐
│  (release/* branch AND REQUIRE_RELEASE_APPROVAL)   │
│                                                     │
│  NO ❌  → Skip to Docker Push                      │
│                                                     │
│  YES ✅ → PAUSE: Wait for Approver                │
│           → Approvers make decision                │
│           → Continue or Abort                      │
└─────────────────────────────────────────────────────┘

┌─ DOCKER PUSH ENABLED? ─────────────────────────────┐
│  (RUN_DOCKER_PUSH parameter = true)                │
│                                                     │
│  NO ❌  → SKIP Docker Push                        │
│                                                     │
│  YES ✅ → Login to Registry + Push Image          │
└─────────────────────────────────────────────────────┘
```

---

## Execution Timeline (Typical Run)

```
Time    Event
────────────────────────────────────────────────────
0:00    CHECKOUT starts
0:10    BUILD starts (after checkout)
2:50    TEST EXECUTION (3 tests in parallel)
        ├─ Unit Test: 0:20 - 0:50
        ├─ Component Test: 0:20 - 1:50
        └─ Blackbox Test: 0:20 - 3:20
3:20    SONARQUBE starts (if conditions met)
3:50    QUALITY GATE (wait for SonarQube)
4:10    SPOTBUGS starts
4:50    JACOCO starts
5:10    DOCKER BUILD (if main/release/tag)
6:30    DOCKER PUSH (if approved on release)
7:00    PUBLISH REPORTS + FINISH
────────────────────────────────────────────────────
Total: ~7 minutes (parallel tests compress time)
```

---

## Test Layer Breakdown

### Layer 1: Unit Tests (Fast Feedback)
```
┌─────────────────────────────────────────────┐
│ UNIT TESTS (~20-30 seconds)                 │
├─────────────────────────────────────────────┤
│ • Service Logic Tests                       │
│ • Mapper Tests (MapStruct validation)       │
│ • Utility Function Tests                    │
│ • Validator Tests                           │
│                                             │
│ 🔧 Setup: No Spring context, No database   │
│ 💾 Data: In-memory mocks                    │
│ 🎯 Scope: Single class in isolation         │
│ 🚀 Speed: Instant feedback                  │
│ ✅ Blocking: YES                            │
└─────────────────────────────────────────────┘
```

**Test count:** ~40-50 unit tests
**Coverage:** Service, mappers, validators (70%+ code)
**Runs:** Every commit, in CI
**Failure impact:** Blocks quality stages

---

### Layer 2: Component Tests (Integration Confidence)
```
┌─────────────────────────────────────────────┐
│ COMPONENT TESTS (~60-90 seconds)            │
├─────────────────────────────────────────────┤
│ • Spring Context Startup                    │
│ • Controller → Service → Repository chain   │
│ • Database Integration (H2 in-memory)       │
│ • Transaction Boundary Tests                │
│ • Flyway Migration Validation               │
│                                             │
│ 🔧 Setup: Full Spring Boot context         │
│ 💾 Data: H2 in-memory database             │
│ 🎯 Scope: Wiring between layers            │
│ 🚀 Speed: Isolated, self-contained         │
│ ✅ Blocking: YES                            │
└─────────────────────────────────────────────┘
```

**Test count:** ~20-30 component tests
**Coverage:** Controllers, services, repos (integration)
**Runs:** Every commit, in CI
**Failure impact:** Blocks quality stages

---

### Layer 3: Blackbox Tests (Behavioral Assurance)
```
┌─────────────────────────────────────────────┐
│ BLACKBOX TESTS (~2-3 minutes)               │
├─────────────────────────────────────────────┤
│ • Embedded Spring Boot App on :8080         │
│ • REST Assured HTTP Calls                   │
│ • JSON Schema Validation                    │
│ • Status Code Assertions                    │
│ • End-to-End Customer Lifecycle             │
│ • Cross-API Workflow Scenarios              │
│                                             │
│ 🔧 Setup: Real embedded application        │
│ 💾 Data: In-process or test container DB   │
│ 🎯 Scope: Externally observable behavior   │
│ 🚀 Speed: Good (uses embedded context)     │
│ ❌ Blocking: NO (marked UNSTABLE on fail)  │
└─────────────────────────────────────────────┘
```

**Test count:** ~30-40 blackbox tests
**Coverage:** API contracts, business flows
**Runs:** Every commit, in CI
**Failure impact:** Non-blocking (allows pipeline to proceed)

---

## Quality Stages Explained

### SonarQube (Code Quality Analysis)
```
Purpose:  Static analysis for code smells and security issues
Triggers: Main + develop + PR + release branches
Timeout:  15 minutes (hardcoded in pipeline)
Metrics:
  • Code coverage percentage
  • Bugs and vulnerabilities
  • Code smells and technical debt
  • Maintainability rating
Skip If:  Unit test OR Component test fails
```

### SpotBugs (Bug Detection)
```
Purpose:  Detect potential bugs from bytecode analysis
Triggers: All commits (if tests pass)
Modules:  main, test, componentTest, blackboxTest
Issues:   
  • Null pointer dereferences
  • Resource leaks
  • Inconsistent synchronization
Skip If:  Unit test OR Component test fails
```

### JaCoCo (Code Coverage)
```
Purpose:  Measure test coverage of source code
Triggers: All commits (if tests pass)
Report:   build/reports/jacoco/test/html/index.html
Metrics:
  • Line coverage %
  • Branch coverage %
  • Method coverage %
Scope:    Unit tests only (not component/blackbox)
```

### Docker Build & Push (Artifact Production)
```
Docker Build:
  Triggers: main branch, release/* branches, tags
  Output:   Docker image tagged with build number
  Example:  bank-customer-onboarding-service:123

Docker Push:
  Skips: Pull requests, develop, feature branches
  Requires: RUN_DOCKER_PUSH=true parameter
  Registry: Configurable via DOCKER_REGISTRY parameter
  Auth: Jenkins credentials (DOCKER_CREDENTIALS_ID)
```

---

## Parameter Reference

| Parameter | Type | Default | Effects |
|-----------|------|---------|---------|
| `RUN_SONAR` | ✅ Boolean | `true` | Include SonarQube stage |
| `RUN_SPOTBUGS` | ✅ Boolean | `true` | Include SpotBugs stage |
| `SONARQUBE_ENV` | 📝 String | `sonarqube` | Jenkins SonarQube env name |
| `DOCKER_IMAGE` | 📝 String | `bank-customer-onboarding-service` | Image name |
| `DOCKER_TAG` | 📝 String | (build #) | Custom tag (e.g., `v1.0.0`) |
| `RUN_DOCKER_PUSH` | ✅ Boolean | `false` | Push to registry |
| `REQUIRE_RELEASE_APPROVAL` | ✅ Boolean | `true` | Approval gate for release/* |
| `DOCKER_REGISTRY` | 📝 String | (DockerHub) | Registry host |
| `DOCKER_CREDENTIALS_ID` | 📝 String | `docker-registry-creds` | Jenkins credentials ID |
| `RELEASE_APPROVERS` | 📝 String | `release-managers,devsecops` | Allowed approvers |

### Example: Custom Parameter Run
```
On "Build Now" button, enter:
  RUN_SONAR = false                    (skip SonarQube)
  DOCKER_TAG = v1.2.3                  (use semantic version)
  RUN_DOCKER_PUSH = true               (push to registry)
  DOCKER_REGISTRY = docker.io/mybank   (push to Docker Hub)
```

---

## Exit Codes and Status

| Status | Color | Meaning | Next Step |
|--------|-------|---------|-----------|
| ✅ SUCCESS | 🟢 Green | All stages passed | Ready to deploy |
| ⚠️ UNSTABLE | 🟡 Yellow | Tests passed, blackbox failed | Investigate + re-run |
| ❌ FAILURE | 🔴 Red | Build or unit/component failed | Fix code, re-run |
| ⏳ TIMEOUT | 🔴 Red | Stage exceeded 60-minute limit | Optimize build |

---

## Common Failure Scenarios

### Scenario 1: Unit Test Failure
```
Expected: Code compiles cleanly
Actual:   Unit test fails

Response:
  1. Pipeline STOPS (blocking gate)
  2. SonarQube SKIPPED (conditional)
  3. SpotBugs SKIPPED (conditional)
  4. Reports published anyway
  5. Engineer fixes code locally and pushes new commit
  6. Pipeline re-runs automatically
```

### Scenario 2: Component Test Passes, Blackbox Fails
```
Expected: API response matches schema
Actual:   Timing issue, port in use, or flaky assertion

Response:
  1. Pipeline marked UNSTABLE 🟡 (non-blocking)
  2. SonarQube completes normally
  3. Quality gates pass
  4. Build proceeds to Docker stage
  5. Engineer reviews blackbox logs separately
  6. Can re-run just blackbox stage manually
```

### Scenario 3: Release Branch Ready to Push
```
When: Push to release/1.0.0 branch
Expected: Approval from release-managers group
Actual: Pipeline pauses at "Release Approval Gate"

Response:
  1. Jenkins UI shows PAUSED stage "Release Approval Gate"
  2. Notifies watchers (email, Slack)
  3. Approver logs into Jenkins → clicks "Approve"
  4. Pipeline resumes, Docker image pushes to registry
  5. Deployment team pulls image from registry
```

---

## Key Insights for Your Demo

🎯 **Emphasize:**
1. **Parallel execution** compresses 8+ sequential stages into ~7 minutes total
2. **Layered tests** provide fast feedback → deeper validation → behavioral assurance
3. **Non-blocking blackbox** allows shipping when unit/component pass, even if UI tests flake
4. **Approval gates** enforce banking governance without halting main delivery
5. **Conditional stages** skip expensive analysis on feature branches
6. **Artifact archiving** makes reports accessible in Jenkins UI for triage

📊 **Show in live demo:**
1. Run blue ocean view to see all stages graphically
2. Click a stage to drill into logs
3. Show test counts and pass/fail breakdown
4. Navigate to JaCoCo report showing coverage %
5. Demonstrate parameter override and custom build
6. Explain why blackbox test timeout is 30min (API load time)


