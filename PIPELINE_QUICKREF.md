# Jenkins Pipeline Quick Start (2-Minute Setup)

## Quick Command Reference

### 1️⃣ Start Jenkins (One Command)
```powershell
cd "D:\bank-customer-onboarding-service"
.\jenkins-quickstart.ps1
```
→ Opens browser to http://localhost:8080
→ Displays initial admin password

### 2️⃣ Initial Jenkins Setup (5 minutes)
- 🔓 Unlock with password from above
- 📦 Install suggested plugins (wait 3-5 min)
- 👤 Create first user: `admin / admin123`
- ✅ Click "Start using Jenkins"

### 3️⃣ Install Extra Plugins (2 minutes)
**Manage Jenkins** → **Manage Plugins** → **Available**

Search and install:
- `Pipeline`
- `Git`
- `Blue Ocean`
- `Junit Plugin`

### 4️⃣ Create Pipeline Job (3 minutes)
- **New Item** → Name: `bank-customer-onboarding-pipeline`
- **Type:** `Pipeline`
- **Pipeline Section:**
  - Definition: `Pipeline script from SCM`
  - SCM: `Git`
  - Repository: `file:///D:/bank-customer-onboarding-service`
  - Script Path: `Jenkinsfile`
- **Save**

### 5️⃣ Run Pipeline
- **Click:** "Build Now"
- **Monitor:** Console Output or Blue Ocean
- **Wait:** ~7 minutes for completion

---

## Understanding Pipeline Stages

| Stage | What It Does | Time | Blocks? |
|-------|--------------|------|---------|
| **Checkout** | Clone git repo | 10s | ✅ Yes |
| **Build** | Compile code | 2min | ✅ Yes |
| **Unit Test** | Fast unit tests | 20s | ✅ Yes |
| **Component Test** | Spring integration | 90s | ✅ Yes |
| **Blackbox Test** | REST API calls | 3min | ❌ No |
| **SonarQube** | Code quality scan | 60s | ✅ Yes |
| **Quality Gate** | Await SQ result | 20s | ✅ Yes |
| **SpotBugs** | Bug detection | 30s | ✅ Yes |
| **JaCoCo** | Coverage report | 20s | ✅ Yes |
| **Docker Build** | Build image | 2min | ✅ Yes |
| **Release Gate** | Manual approval | ⏳ | ✅ Yes |
| **Docker Push** | Push to registry | 1min | ✅ Yes |
| **Reports** | Archive artifacts | 10s | ✅ Yes |

**Running time: ~7 minutes** (parallel execution compresses duration)

---

## What Each Test Layer Tests

```
┌────────────────────────────────────────────────────────┐
│ UNIT TESTS (20-30 seconds)                            │
│ ✓ Service logic, mappers, validators                  │
│ ✓ No database, no Spring context                      │
│ ✓ ~50 tests, instant feedback                         │
│ ✓ BLOCKING: If fails, pipeline stops                  │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│ COMPONENT TESTS (60-90 seconds)                       │
│ ✓ Spring wiring, controller → service → repo          │
│ ✓ H2 in-memory database                               │
│ ✓ ~30 tests, validates integration                    │
│ ✓ BLOCKING: If fails, pipeline stops                  │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│ BLACKBOX TESTS (2-3 minutes)                          │
│ ✓ Real HTTP calls to embedded REST API                │
│ ✓ JSON schema validation                              │
│ ✓ ~40 tests, validates behavior                       │
│ ✓ NON-BLOCKING: If fails, pipeline marks UNSTABLE     │
└────────────────────────────────────────────────────────┘
```

---

## Docker Setup Includes

**Containers started by `jenkins-quickstart.ps1`:**

1. **Jenkins Container**
   - Image: `jenkins/jenkins:2.426.3-lts-jdk21`
   - UI Port: `8080` (http://localhost:8080)
   - Agent Port: `50000`
   - Volumes: `/var/jenkins_home` (persisted)

2. **PostgreSQL Container**
   - Image: `postgres:15-alpine`
   - Port: `5432`
   - Database: `bank_onboarding`
   - User: `bank_user` / `bank_password`

---

## Viewing Results

### After Pipeline Completes:

**Test Results:**
- http://localhost:8080 → Build → Test Result

**Coverage Report:**
- Build page → Artifacts → `build/reports/jacoco/test/html/index.html`

**Aggregate Report:**
- Build page → Artifacts → `build/reports/tests/layers/index.html`

**Blue Ocean (Better View):**
- http://localhost:8080/blue → Click pipeline → Click build

---

## Common Commands

### Show Jenkins logs
```powershell
docker logs jenkins-pipeline -f
```

### Stop all containers
```powershell
docker compose -f docker-compose-jenkins.yml down
```

### Clean and restart
```powershell
docker compose -f docker-compose-jenkins.yml down -v
.\jenkins-quickstart.ps1
```

### Access Jenkins CLI
```powershell
docker exec jenkins-pipeline jenkins-cli -s http://localhost:8080 who-am-i
```

---

## Parameters for Advanced Builds

When you click **"Build Now"** → **"Build with Parameters"**:

| Parameter | Default | Example Override |
|-----------|---------|-------------------|
| `RUN_SONAR` | `true` | `false` |
| `RUN_SPOTBUGS` | `true` | `false` |
| `DOCKER_TAG` | (build #) | `v1.2.3` |
| `RUN_DOCKER_PUSH` | `false` | `true` |
| `DOCKER_REGISTRY` | (empty) | `docker.io/myorg` |

---

## Exit Statuses

| Status | What it means | How to respond |
|--------|---------------|----------------|
| 🟢 **SUCCESS** | All stages passed | Deploy! |
| 🟡 **UNSTABLE** | Tests passed, blackbox failed | Investigation optional |
| 🔴 **FAILURE** | Build or unit/component failed | Fix code and re-run |

---

## Troubleshooting

### Q: Can't connect to localhost:8080
**A:** Jenkins still starting. Wait 1-2 minutes. Check: `docker ps`

### Q: "Docker command not found" error
**A:** Run this once:
```powershell
docker exec -u root jenkins-pipeline usermod -aG docker jenkins
docker restart jenkins-pipeline
```

### Q: Port 5432 already in use
**A:** Edit `docker-compose-jenkins.yml` line:
```yaml
ports:
  - "5433:5432"  # Change from 5432
```

### Q: Out of memory errors
**A:** Edit `docker-compose-jenkins.yml`:
```yaml
JAVA_OPTS: "-Xmx2048m -Xms1024m"  # Increase from 1024m
```

---

## Key Takeaways

✅ **Layered tests** = Fast feedback (unit) + Integration validation (component) + API behavior (blackbox)

✅ **Parallel execution** = Compresses time despite 8+ stages

✅ **Non-blocking blackbox** = Pipeline proceeds even if UI tests flake (common in real environments)

✅ **Approval gates** = Governance for banking without blocking delivery

✅ **Quality-first** = Expensive analysis (SonarQube) skips if fast tests fail

✅ **Comprehensive reports** = Visibility for engineers and release managers

---

## Next Steps

1. ✅ Run `.\jenkins-quickstart.ps1`
2. ✅ Complete Jenkins setup (steps 2-4 above)
3. ✅ Create Pipeline job pointing to Jenkinsfile
4. ✅ Click "Build Now" and watch it run
5. ✅ Review test results and coverage report
6. ✅ Read `JENKINS_SETUP.md` for detailed walkthrough
7. ✅ Read `PIPELINE_FLOW.md` for architecture details

---

## Related Files

- 📄 `JENKINS_SETUP.md` - Detailed 14-step setup guide
- 📄 `PIPELINE_FLOW.md` - Visual pipeline architecture and flow diagrams
- ⚙️ `Jenkinsfile` - Pipeline definition (what stages run)
- 🐳 `docker-compose-jenkins.yml` - Docker containers for Jenkins + PostgreSQL
- ⚡ `jenkins-quickstart.ps1` - Automated setup script


