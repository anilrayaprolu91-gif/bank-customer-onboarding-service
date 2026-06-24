# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and dependency manifests first (layer-cache friendly)
COPY gradlew                    ./
COPY gradle/                    ./gradle/
COPY build.gradle               ./
COPY settings.gradle            ./
COPY gradle.properties          ./

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet || true

# Copy source and build
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="platform-engineering@bank.com"
LABEL app="bank-customer-onboarding-service"
LABEL version="1.0.0"

# Security: run as non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
USER appuser

# Copy the fat JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# JVM tuning for containerised environments
ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -XX:+ExitOnOutOfMemoryError \
  -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

