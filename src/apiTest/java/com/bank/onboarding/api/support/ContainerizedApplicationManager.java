package com.bank.onboarding.api.support;

import com.bank.onboarding.api.config.EnvironmentConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public final class ContainerizedApplicationManager {

    private static final Object MONITOR = new Object();

    private static Network network;
    private static PostgreSQLContainer<?> postgres;
    private static GenericContainer<?> application;
    private static String baseUrl;

    private ContainerizedApplicationManager() {
    }

    public static String start(EnvironmentConfig config) {
        synchronized (MONITOR) {
            if (application != null && application.isRunning()) {
                return baseUrl;
            }

            network = Network.newNetwork();
            postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName(config.dbName())
                    .withUsername(config.dbUsername())
                    .withPassword(config.dbPassword())
                    .withNetwork(network)
                    .withNetworkAliases("postgres");
            postgres.start();

            ImageFromDockerfile image = new ImageFromDockerfile("bank-customer-onboarding-api-tests:latest", false)
                    .withDockerfile(Path.of("Dockerfile"))
                    .withFileFromPath(".", Path.of("."));

            application = new GenericContainer<>(image)
                    .withExposedPorts(8080)
                    .withNetwork(network)
                    .withEnv("DB_HOST", "postgres")
                    .withEnv("DB_PORT", "5432")
                    .withEnv("DB_NAME", config.dbName())
                    .withEnv("DB_USERNAME", config.dbUsername())
                    .withEnv("DB_PASSWORD", config.dbPassword())
                    .withEnv("APP_KAFKA_ENABLED", "false")
                    .withEnv("SPRING_PROFILES_ACTIVE", "docker")
                    .waitingFor(Wait.forHttp("/actuator/health")
                            .forPort(8080)
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(8)));
            application.start();

            baseUrl = "http://" + application.getHost() + ":" + application.getMappedPort(8080);
            return baseUrl;
        }
    }
}

