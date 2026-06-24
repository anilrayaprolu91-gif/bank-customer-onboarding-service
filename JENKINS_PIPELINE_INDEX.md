# 📚 Jenkins Pipeline Documentation Index

## START HERE 👇

**New to this pipeline?** Read these in order:

### 1. 🚀 **Quick Start** (2 min)
**File:** `RUNNING_JENKINS_LOCALLY.md`
- One-command setup
- What happens
- Next steps

### 2. ⚡ **Quick Reference** (5 min)
**File:** `PIPELINE_QUICKREF.md`
- Stage table
- Test layer breakdown
- Common commands
- Troubleshooting

### 3. 📊 **Pipeline Architecture** (15 min)
**File:** `PIPELINE_FLOW.md`
- Visual flow diagram (ASCII)
- Stage-by-stage explanation
- Execution timeline
- Failure scenarios

### 4. 🛠️ **Detailed Setup** (30 min)
**File:** `JENKINS_SETUP.md`
- 14 detailed steps
- Every menu click explained
- Time breakdown
- Parameter reference

---

## 🎯 What Does This Pipeline Do?

Your Jenkins pipeline:
- **Builds** your Spring Boot 3 microservice
- **Tests** with 3 layers: unit (fast) → component (integration) → blackbox (API)
- **Analyzes** code quality (SonarQube), detects bugs (SpotBugs), measures coverage (JaCoCo)
- **Builds** Docker image
- **Requires approval** before Docker push (governance)
- **Archives** all reports
- **All in ~7 minutes** (parallel execution)

---

## 🔥 Right Now: Get Started

### Option A: Fastest Path (Automated)
```powershell
cd "D:\bank-customer-onboarding-service"
.\jenkins-quickstart.ps1
```
This starts Jenkins in Docker and shows next steps.

### Option B: Manual Path (Understand Each Step)
1. Read `RUNNING_JENKINS_LOCALLY.md`
2. Follow step-by-step instructions
3. Learn what each stage does

---

## 📋 All Documents

| Document | Audience | Time | Content |
|----------|----------|------|---------|
| `RUNNING_JENKINS_LOCALLY.md` | Everyone | 5 min | Overview + quick start + next steps |
| `PIPELINE_QUICKREF.md` | Operators | 5 min | Reference tables, commands, troubleshooting |
| `PIPELINE_FLOW.md` | Engineers | 15 min | Architecture, flow diagrams, scenarios |
| `JENKINS_SETUP.md` | Detailed learners | 30 min | Full walkthrough of every Jenkins menu |

---

## 🐳 What's Running in Docker

When you start the pipeline:

**Jenkins Container**
- Port: 8080
- URL: http://localhost:8080
- Login: admin / admin123 (after setup)

**PostgreSQL Container**
- Port: 5432
- Database: bank_onboarding
- User: bank_user / bank_password

---

## 📊 Pipeline Stages Overview

```
12 Stages Running in ~7 Minutes
├─ Stage 1: Checkout (10s)
├─ Stage 2: Build (2m)
├─ Stages 3a, 3b, 3c: Tests (PARALLEL, 3m total)
│  ├─ Unit Tests (20s)
│  ├─ Component Tests (90s)
│  └─ Blackbox Tests (3m) [NON-BLOCKING]
├─ Stage 4: SonarQube (60s) [if tests pass]
├─ Stage 5: Quality Gate (20s) [if tests pass]
├─ Stage 6: SpotBugs (30s) [if tests pass]
├─ Stage 7: JaCoCo (20s) [if tests pass]
├─ Stage 8: Docker Build (2m) [main/release only]
├─ Stage 9: Release Approval (⏳ manual) [release/* only]
├─ Stage 10: Docker Push (1m) [if approved]
├─ Stage 11: Quality Summary (2s)
└─ Stage 12: Publish Reports (10s)
```

---

## ✅ Quality Gates (Why Some Stages Are Skipped)

### Gate 1: Tests Must Pass
```
If Unit OR Component test fails:
  ❌ Pipeline STOPS
  ❌ SonarQube SKIPPED
  ❌ SpotBugs SKIPPED
  ❌ Nothing gets released
```

### Gate 2: Code Quality Must Pass
```
If SonarQube quality gate fails:
  ❌ Pipeline STOPS (doesn't proceed to Docker)
  ✅ But test reports still published
```

### Gate 3: Release Approval
```
If pushing to release/* branch:
  ⏳ Pipeline PAUSES
  👥 Waits for: release-managers or devsecops group
  ✅ Approver clicks "Approve"
  ❌ Approver clicks "Abort"
```

---

## 🎯 Why This Pipeline is Built This Way

| Feature | Why |
|---------|-----|
| **Layered tests** | Unit tests catch bugs fast; component tests validate wiring; blackbox tests validate API behavior |
| **Parallel execution** | Unit + Component + Blackbox run together → 7 min vs 5+ hours sequential |
| **Non-blocking blackbox** | Real-world API/UI tests can be flaky; don't want to stop shipping for infrastructure flakiness |
| **SonarQube gate** | Prevents technical debt accumulation; banking code needs to be maintainable |
| **Release approval** | Governance: only authorized people can push to production |
| **Docker tagging** | Automated (build #) by default; can override with semantic version (v1.2.3) |
| **Selective SonarQube** | Don't run expensive analysis on every feature branch; only main/develop/PR/release |

---

## 🔍 How to Monitor Your Pipeline

### In Browser
```
http://localhost:8080
  → Blue Ocean (better view)
    → bank-customer-onboarding-pipeline
      → Build #1, #2, etc.
        → See stages graphically
        → Click stage for logs
        → See test counts
```

### In Terminal
```powershell
# See Jenkins startup logs
docker logs jenkins-pipeline -f

# Get list of builds
docker exec jenkins-pipeline jenkins-cli -s http://localhost:8080 list-jobs
```

---

## 🛑 When Pipeline Fails

### Scenario 1: Unit Test Fails
```
Expected: Code compiles and tests pass
Actual: One unit test fails

What happens:
  ❌ Pipeline stops
  📝 Test output shows which test failed
  🔧 Engineer fixes code locally
  ⬆️ Engineer pushes new commit
  🔄 Pipeline re-runs automatically
```

### Scenario 2: Blackbox Test Fails
```
Expected: API returns 200 OK
Actual: API returns 500 Error

What happens:
  🟡 Pipeline marked UNSTABLE (not RED)
  ✅ SonarQube still runs
  ✅ SpotBugs still runs
  ✅ Docker image still builds
  🔍 Engineer reviews blackbox logs separately
  ↻ Can re-run just the blackbox stage later
```

### Scenario 3: Release Approval Pending
```
Expected: Push to release/1.0.0 branch
Actual: Pipeline pauses at Release Approval Gate

What happens:
  ⏳ Pipeline shows PAUSED state
  📬 Approvers get notification (email/Slack)
  👤 Approver logs into Jenkins
  ✅ Clicks "Approve" button
  🚀 Pipeline resumes and pushes Docker image
```

---

## 💾 Files You Got

**Docker Setup:**
- `docker-compose-jenkins.yml` — Starts Jenkins + PostgreSQL

**Automation:**
- `jenkins-quickstart.ps1` — One-click startup

**Documentation (NEW!):**
- `RUNNING_JENKINS_LOCALLY.md` ← READ THIS FIRST
- `PIPELINE_QUICKREF.md` ← Quick reference
- `PIPELINE_FLOW.md` ← Architecture & diagrams
- `JENKINS_SETUP.md` ← Detailed walkthrough
- `JENKINS_PIPELINE_INDEX.md` ← This file

---

## 🚀 Get Started Now

### Command 1: Start Jenkins
```powershell
cd "D:\bank-customer-onboarding-service"
.\jenkins-quickstart.ps1
```

**Output will show:**
- Jenkins URL: http://localhost:8080
- Initial admin password
- Next steps to follow

### Command 2: (Optional) Stop Jenkins
```powershell
docker compose -f docker-compose-jenkins.yml down
```

### Command 3: (Optional) Full restart
```powershell
docker compose -f docker-compose-jenkins.yml down -v
.\jenkins-quickstart.ps1
```

---

## 🎓 Key Concepts

**Test Pyramid:**
```
        Blackbox (API/UI)  ← Slower, behavior-focused
       Component (Spring)   ← Medium speed, integration
      Unit (Logic)          ← Fast, unit-focused
```

**Pipeline Principle:**
```
Fail Fast → Fast Feedback → Ship Regularly
```

**Quality Levels:**
```
Red (Broken)    → Unit/Component test fails
Yellow (Caution) → Blackbox test fails, but integration OK
Green (Go)      → All gates pass, ready to deploy
```

---

## 📞 Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| Can't open http://localhost:8080 | Jenkins still starting (wait 1-2 min) |
| Docker command not found in Jenkins | Run: `docker exec -u root jenkins-pipeline usermod -aG docker jenkins` |
| Port 8080 already in use | Stop other app or change docker-compose mapping |
| Out of memory | Increase `JAVA_OPTS` in docker-compose-jenkins.yml |
| Forgot initial password | Check: `docker exec jenkins-pipeline cat /var/jenkins_home/secrets/initialAdminPassword` |

---

## 🎯 What You'll Learn

After running this pipeline, you'll understand:
- ✅ How Jenkins orchestrates multi-stage builds
- ✅ Why tests are layered and parallelized
- ✅ How Docker integration works
- ✅ How approval gates enforce governance
- ✅ How to monitor builds and troubleshoot failures
- ✅ How to customize pipeline behavior with parameters

---

## 📖 Further Reading

- Jenkins Pipeline Docs: https://www.jenkins.io/doc/book/pipeline/
- Spring Boot 3 Guide: https://spring.io/projects/spring-boot
- Docker Compose: https://docs.docker.com/compose/
- Your Project: See `README.md` and `TEST-LAYERS.md`

---

## ✨ Next Steps

**NOW:** Run `.\jenkins-quickstart.ps1`

**NEXT:** Read `RUNNING_JENKINS_LOCALLY.md` (5 min)

**THEN:** Complete Jenkins setup (10 min)

**FINALLY:** Click "Build Now" and watch your pipeline run (7 min)

---

**Happy building! 🚀**


