# Enterprise Test Layering (Gradle)

This project uses four verification layers aligned with enterprise banking delivery pipelines.

## Layers

- `test`: fast unit tests
- `componentTest`: Spring-context component tests
- `blackboxTest`: blackbox API automation (Rest Assured + schema + WireMock + Testcontainers)
- `consumerContractTest`: consumer-driven contract tests (Pact)

## Source Sets

Configured in `build.gradle`:

- `src/test/java`, `src/test/resources` for `test`
- `src/componentTest/java`, `src/componentTest/resources` for `componentTest`
- `src/apiTest/java`, `src/apiTest/resources` for `blackboxTest`
- `src/consumerContractTest/java`, `src/consumerContractTest/resources` for `consumerContractTest`

## Execution Flow

Flow is controlled in Gradle:

1. `test`
2. `componentTest` (`shouldRunAfter test`)
3. `blackboxTest` (`shouldRunAfter componentTest`)
4. `consumerContractTest` (`shouldRunAfter test`)

`check` depends on all four layers.

## Reporting

Per-layer reports:

- `build/reports/tests/test`
- `build/reports/tests/componentTest`
- `build/reports/tests/blackboxTest`
- `build/reports/tests/consumerContractTest`

Contract artifacts:

- Provider verification results: `build/test-results/contractTest`
- Consumer pact files: `build/pacts`

Aggregate report task:

- `testLayersReport` -> `build/reports/tests/layers`

## Run Commands (PowerShell)

```powershell
Set-Location "D:\bank-customer-onboarding-service"
.\gradlew.bat test --no-daemon
.\gradlew.bat contractTest --no-daemon
.\gradlew.bat componentTest --no-daemon
.\gradlew.bat blackboxTest --no-daemon
.\gradlew.bat consumerContractTest --no-daemon
.\gradlew.bat check testLayersReport --no-daemon
```

