package com.bank.onboarding.api.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.UUID;

public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        if (requestSpec.getHeaders().hasHeaderWithName(CORRELATION_HEADER)) {
            return context.next(requestSpec, responseSpec);
        }

        requestSpec.header(CORRELATION_HEADER, UUID.randomUUID().toString());
        return context.next(requestSpec, responseSpec);
    }
}

