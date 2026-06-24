# Kafka Demo Slice

This project now includes a minimal real Kafka flow:

- `CustomerServiceImpl` publishes `CustomerOnboardedEvent` after successful onboarding.
- `CustomerOnboardedKafkaListener` consumes the same topic.
- `ConsumedOnboardingEventStore` keeps consumed events for assertions.
- `KafkaCustomerOnboardingComponentTest` runs a real Kafka broker using Testcontainers.
- `KafkaOnboardingBlackboxApiTest` calls the onboarding API and verifies Kafka consumption in the same test.
- `KafkaDisabledOnboardingBlackboxApiTest` verifies onboarding still succeeds when Kafka is disabled and no consumed event is recorded.

## Local test run

```powershell
Set-Location "D:\bank-customer-onboarding-service"
.\gradlew.bat componentTest --tests "*KafkaCustomerOnboardingComponentTest" --no-daemon
.\gradlew.bat blackboxTest --tests "*KafkaOnboardingBlackboxApiTest" --no-daemon
.\gradlew.bat blackboxTest --tests "*KafkaDisabledOnboardingBlackboxApiTest" --no-daemon
```

The blackbox Kafka test is annotated with `@Testcontainers(disabledWithoutDocker = true)` and will be skipped when Docker is unavailable.

## Docker profile run (app + postgres + kafka)

```powershell
Set-Location "D:\bank-customer-onboarding-service"
docker compose up -d postgres kafka app
```

Kafka defaults in Docker profile:

- `APP_KAFKA_ENABLED=true`
- `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`
- topic: `customer.onboarded.v1`

To disable Kafka quickly for a Docker run:

```powershell
docker compose run --rm -e APP_KAFKA_ENABLED=false app
```

