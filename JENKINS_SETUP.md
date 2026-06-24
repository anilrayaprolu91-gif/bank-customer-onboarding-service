# Jenkins Pipeline Setup Guide for Bank Customer Onboarding Service

## Prerequisites
- Docker and Docker Compose installed
- Git installed (for the project repository)
- 8GB RAM available

---

## Step 1: Start Jenkins and PostgreSQL with Docker Compose

```powershell
Set-Location "D:\bank-customer-onboarding-service"
docker compose -f docker-compose-jenkins.yml up -d
```

**Wait for Jenkins to be ready (~30-60 seconds):**
```powershell
docker logs jenkins-pipeline -f
```

Watch for: `'Jenkins is fully up and running'`

Press `Ctrl+C` to exit logs.

---

## Step 2: Access Jenkins

Open browser to: **http://localhost:8080**

**Get initial admin password:**
```powershell
docker exec jenkins-pipeline cat /var/jenkins_home/secrets/initialAdminPassword
```

Copy the password and use it to unlock Jenkins.

---

## Step 3: Initial Jenkins Setup

1. **Paste** the initial admin password
2. **Click** "Install suggested plugins"
3. **Wait** for plugins to install (~3-5 minutes)
4. **Create** first admin user:
   - Username: `admin`
   - Password: `admin123` (or your choice)
5. **Click** "Save and Continue"
6. **Instance Configuration** → Click "Save and Finish"
7. **Click** "Start using Jenkins"

---

## Step 4: Install Additional Plugins

1. **Dashboard** → **Manage Jenkins** → **Manage Plugins**
2. **Available Plugins** tab → Search and install each:
   - `Pipeline`
   - `Git`
   - `Blue Ocean`
   - `SonarQube Scanner` (optional for this demo)
   - `Junit Plugin` (for test results)
   - `Allure Plugin` (optional for Allure reports)

3. **Click** "Install without restart"
4. **Wait** for installation to complete
5. **Check** "Restart Jenkins when installation is complete"

Jenkins will restart (~1 minute).

---

## Step 5: Create a New Pipeline Job

1. **Dashboard** → **New Item**
2. **Item name**: `bank-customer-onboarding-pipeline`
3. **Select**: `Pipeline`
4. **Click** "OK"

---

## Step 6: Configure Pipeline from Git

In the job configuration:

### General
- **Check**: "GitHub project" (optional, if hosted on GitHub)
- **Project URL**: `https://github.com/YOUR_ORG/bank-customer-onboarding-service`

### Pipeline Section
- **Definition**: `Pipeline script from SCM`
- **SCM**: `Git`
- **Repository URL**: `https://github.com/YOUR_ORG/bank-customer-onboarding-service.git` 
  - OR for local file system: `file:///D:/bank-customer-onboarding-service`
- **Credentials**: Leave as-is (for public repo) or add GitHub credentials
- **Branch**: `*/main` (or your branch)
- **Script Path**: `Jenkinsfile`

### Save
**Click** "Save"

---

## Step 7: Run the Pipeline Manually

1. **Click** "Build Now"
2. **Wait** for build to start
3. **Click** the build number (e.g., `#1`) in Build History
4. **Click** "Console Output" to see live logs

---

## Step 8: Understanding the Pipeline Execution

The Jenkinsfile has **8 main stages**. Here's what each does:

### Stage 1: **Checkout**
```groovy
checkout scm  // Clones the repository
env.EFFECTIVE_DOCKER_TAG = params.DOCKER_TAG ?: env.BUILD_NUMBER
```
- **Duration**: ~10 seconds
- **What happens**: Git clones the repo, sets up Docker image naming
- **Success look**: `Cloning into 'workspace'...`

---

### Stage 2: **Build**
```groovy
./gradlew clean assemble -x test --no-daemon
```
- **Duration**: ~2-3 minutes (first run slower due to dependency download)
- **What happens**: 
  - Cleans old build artifacts
  - Compiles Java source code
  - Runs annotation processors (MapStruct, OpenAPI)
  - Creates JAR file without running tests
- **Success look**: `BUILD SUCCESSFUL`

---

### Stage 3: **Test Execution** (Parallel - runs 3 layers at once)

#### 3a. **Unit Test**
```groovy
./gradlew test --no-daemon
```
- **Duration**: ~20-30 seconds
- **What happens**:
  - Runs ~40-50 unit tests in isolation
  - No Spring context, no database
  - Tests service, mapper, and utility logic
- **Output**: Red/green test list
- **Sets**: `UNIT_TEST_PASSED = true/false`

#### 3b. **Component Test**
```groovy
./gradlew componentTest --no-daemon
```
- **Duration**: ~60-90 seconds
- **What happens**:
  - Boots full Spring context with H2 in-memory database
  - Tests controller layers, service wiring, repository behavior
  - Validates transactional boundaries and Spring integration
- **Output**: Test results and database migration logs
- **Sets**: `COMPONENT_TEST_PASSED = true/false`

#### 3c. **Blackbox Test** (Non-blocking - marked UNSTABLE if fails)
```groovy
./gradlew blackboxTest --no-daemon
```
- **Duration**: ~2-3 minutes
- **What happens**:
  - Starts embedded Spring Boot application on port 8080
  - Runs REST Assured tests against live API
  - Validates HTTP status codes, JSON schemas, response contracts
  - Tests end-to-end customer onboarding flows
- **Output**: API request/response details and assertion results
- **Note**: Failures don't stop the pipeline

**Gates that follow depend on:** `UNIT_TEST_PASSED == 'true' && COMPONENT_TEST_PASSED == 'true'`

---

### Stage 4: **SonarQube** (Conditional - skipped if unit/component fail)
```groovy
./gradlew sonar --no-daemon
```
- **Duration**: ~30-60 seconds
- **What happens**: Sends code metrics to SonarQube
  - Code coverage percentage
  - Code smells, bugs, vulnerabilities
  - Technical debt ratio
- **Runs only on**: `main`, `develop`, `release/*`, or pull requests

---

### Stage 5: **SonarQube Quality Gate** (Conditional)
```groovy
waitForQualityGate abortPipeline: true
```
- **Duration**: ~10-20 seconds
- **What happens**: Polls SonarQube until analysis completes
  - If quality gate PASSES: Pipeline continues ✅
  - If quality gate FAILS: Pipeline stops ❌
- **Runs only on**: Same branches as SonarQube stage

---

### Stage 6: **SpotBugs** (Conditional - if unit/component pass)
```groovy
./gradlew spotbugsMain spotbugsTest spotbugsComponentTest spotbugsBlackboxTest
```
- **Duration**: ~20-40 seconds
- **What happens**: Static analysis finds potential bugs
  - Null pointer dereferences
  - Resource leaks
  - Inconsistent synchronization
- **Runs only if**: Unit AND Component tests passed

---

### Stage 7: **JaCoCo Code Coverage Report** (Conditional)
```groovy
./gradlew jacocoTestReport --no-daemon
```
- **Duration**: ~10-20 seconds
- **What happens**: Generates code coverage report from unit test execution
  - Shows % of lines/branches covered by tests
  - Identifies uncovered code
- **Output**: HTML report at `build/reports/jacoco/test/html/index.html`

---

### Stage 8: **Docker Build** (Conditional - only on main, release/*, or tags)
```groovy
docker build -t ${EFFECTIVE_DOCKER_IMAGE_TAG} .
```
- **Duration**: ~1-2 minutes
- **What happens**: 
  - Builds Docker image from Dockerfile
  - Tags with build number or custom tag
- **Runs only on**: `main`, `release/*`, or tag builds

---

### Stage 9: **Release Approval Gate** (Conditional - release branch)
```groovy
input message: "Approve Docker push?"
submitter: 'release-managers,devsecops'
```
- **Duration**: ⏳ **Waits indefinitely** for manual approval
- **What happens**:
  - Pauses pipeline
  - Sends approval request to named Jenkins users/groups
  - Approvers click "Approve" to proceed
  - Rejecter can abort
- **Runs only on**: `release/*` branches when `REQUIRE_RELEASE_APPROVAL=true`

---

### Stage 10: **Docker Push** (Conditional)
```groovy
docker push ${EFFECTIVE_DOCKER_IMAGE_TAG}
```
- **Duration**: ~30-60 seconds
- **What happens**:
  - Logs into Docker registry
  - Pushes built image to registry
  - Logs out securely
- **Runs only if**: `RUN_DOCKER_PUSH=true` AND (main branch OR release/* OR tag)

---

### Stage 11: **Quality Gate Summary**
```groovy
echo "UNIT_TEST_PASSED=${UNIT_TEST_PASSED}, COMPONENT_TEST_PASSED=${COMPONENT_TEST_PASSED}..."
```
- **Duration**: ~2 seconds
- **What happens**: Outputs flags showing which gates passed/failed
- **Purpose**: Clear visibility into why quality stages were skipped

---

### Stage 12: **Publish Reports**
```groovy
./gradlew testLayersReport --no-daemon
archiveArtifacts artifacts: 'build/reports/**, build/test-results/**'
```
- **Duration**: ~10 seconds
- **What happens**:
  - Generates aggregate test report across all layers
  - Archives test results, Allure reports, JaCoCo reports
  - Makes them available for download in Jenkins UI
- **Always runs** (even if tests fail)

---

## Step 9: View Pipeline Results in Jenkins UI

### Option 1: Classic View
1. **Click** the build number
2. **Click** "Console Output" for live logs
3. **Scroll** to find specific stage output

### Option 2: Blue Ocean (Better visualization)
1. **Sidebar** → **Blue Ocean**
2. **Click** the pipeline
3. **Click** a build to see stages graphically
4. **Click** a stage to see its logs
5. **Color scheme**:
   - 🟢 **Green** = Passed
   - 🟡 **Yellow** = Unstable (blackbox tests, warnings)
   - 🔴 **Red** = Failed

---

## Step 10: View Test Reports

After pipeline completes:

### Test Reports
1. **Build page** → **Test Result** (shows counts)
2. **Click** individual test to see failure details

### Coverage Report
1. **Artifacts** → **build/reports/jacoco/test/html/index.html**

### Aggregate Test Report
1. **Artifacts** → **build/reports/tests/layers/index.html**

---

## Step 11: Parameterize the Pipeline (Optional)

To customize pipeline behavior:

1. **Job** → **Configure**
2. **General** → **Check** "This project is parameterized"
3. **Add** parameters matching the Jenkinsfile:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `RUN_SONAR` | Boolean | true | Run SonarQube analysis |
| `RUN_SPOTBUGS` | Boolean | true | Run SpotBugs |
| `DOCKER_TAG` | String | (empty) | Custom Docker tag (defaults to build number) |
| `RUN_DOCKER_PUSH` | Boolean | false | Push Docker image to registry |
| `DOCKER_REGISTRY` | String | (empty) | Registry URL (e.g., docker.io/myorg) |

4. **Save**
5. **Build Now** → Parameters appear with defaults
6. **Override** and build

---

## Step 12: Troubleshooting Common Issues

### Problem: "Docker command not found"
**Solution**: Add Jenkins container user to docker group:
```powershell
docker exec -u root jenkins-pipeline usermod -aG docker jenkins
docker restart jenkins-pipeline
```

### Problem: "Out of memory" during build
**Solution**: Increase JVM memory in docker-compose-jenkins.yml:
```yaml
JAVA_OPTS: "-Xmx2048m -Xms1024m"  # Change from 1024m to 2048m
```
Then restart: `docker compose -f docker-compose-jenkins.yml restart jenkins`

### Problem: PostgreSQL port already in use
**Solution**: Change docker-compose port mapping:
```yaml
postgres:
  ports:
    - "5433:5432"  # Change 5432 to 5433
```

### Problem: Jenkinsfile not found
**Solution**: Ensure `Script Path` in Pipeline config is set to `Jenkinsfile` (not `Jenkinsfile` with path)

---

## Step 13: Pipeline Execution Time Breakdown

| Stage | Time | Blocking |
|-------|------|----------|
| Checkout | ~10s | ✅ Yes |
| Build | ~2-3 min | ✅ Yes |
| **Unit Test** | ~20-30s | ✅ Yes |
| **Component Test** | ~60-90s | ✅ Yes |
| **Blackbox Test** | ~2-3 min | ❌ No |
| SonarQube | ~30-60s | ✅ Yes |
| Quality Gate | ~10-20s | ✅ Yes |
| SpotBugs | ~20-40s | ✅ Yes |
| JaCoCo | ~10-20s | ✅ Yes |
| Docker Build | ~1-2 min | ✅ Yes |
| Reports & Archive | ~10s | ✅ Yes |
| **Total (typical)** | **~8-12 min** | — |

*(Tests run in parallel, so total is compressed)*

---

## Step 14: Run a Complete Pipeline Example

```powershell
# 1. Start Docker containers
Set-Location "D:\bank-customer-onboarding-service"
docker compose -f docker-compose-jenkins.yml up -d

# 2. Wait for Jenkins to be ready (~1 minute)
Start-Sleep -Seconds 60

# 3. Open browser
Start-Process "http://localhost:8080"

# 4. Follow steps 2-6 above to configure

# 5. Trigger pipeline via CLI (optional):
# docker exec jenkins-pipeline jenkins-cli -s http://localhost:8080 build bank-customer-onboarding-pipeline

# 6. Watch logs
docker logs jenkins-pipeline -f

# 7. View results at http://localhost:8080/blue/organizations/jenkins/bank-customer-onboarding-pipeline/activity
```

---

## Key Takeaways

✅ **Three test layers** (unit, component, blackbox) provide layered confidence
✅ **Parallel execution** compresses time despite having 8+ stages
✅ **Non-blocking blackbox** lets the pipeline proceed even if UI tests are flaky
✅ **Approval gates** enforce banking governance on release branches
✅ **Comprehensive reports** provide visibility into coverage, quality, and behavior
✅ **Docker integration** makes the pipeline reproducible and portable


