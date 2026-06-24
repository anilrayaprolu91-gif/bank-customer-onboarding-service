# API Automation Framework

Rest Assured-based API automation suite for the bank customer onboarding microservice.

## Highlights

- JUnit 5
- Rest Assured
- Jackson object mapping
- AssertJ assertions
- Allure integration
- JSON schema validation
- WireMock support
- Testcontainers-powered environment bootstrapping
- Request builder pattern
- Factory pattern
- Environment profiles
- Logging filters

## Profiles

- `embedded` (default): starts the Spring Boot microservice in-process with the `test` profile and H2
- `container`: starts PostgreSQL and the Spring Boot app in Testcontainers
- `local`: targets an already-running application at `http://localhost:8080`
- `mock`: reserved for WireMock-only flows

## Run the API automation suite

```powershell
Set-Location "D:\bank-customer-onboarding-service"
.\gradlew.bat apiTest --no-daemon
```

Run against a locally running app:

```powershell
Set-Location "D:\bank-customer-onboarding-service"
.\gradlew.bat apiTest --no-daemon -Dapi.profile=local
```

