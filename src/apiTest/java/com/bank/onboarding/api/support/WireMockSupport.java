package com.bank.onboarding.api.support;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public final class WireMockSupport implements AutoCloseable {

    private final WireMockServer server;

    public WireMockSupport() {
        this.server = new WireMockServer(options().dynamicPort());
    }

    public void start() {
        server.start();
    }

    public String baseUrl() {
        return server.baseUrl();
    }

    public void stubRiskCountry(String countryCode, String riskLevel, int riskScore) {
        server.stubFor(get(urlEqualTo("/reference/risk-countries/" + countryCode))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "country": "%s",
                                  "riskLevel": "%s",
                                  "riskScore": %d
                                }
                                """.formatted(countryCode, riskLevel, riskScore))));
    }

    @Override
    public void close() {
        server.stop();
    }
}

