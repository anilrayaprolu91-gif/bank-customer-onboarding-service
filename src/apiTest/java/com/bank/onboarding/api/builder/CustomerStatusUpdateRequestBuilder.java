package com.bank.onboarding.api.builder;

import com.bank.onboarding.api.model.request.CustomerStatusUpdateRequest;

public final class CustomerStatusUpdateRequestBuilder {

    private String status = "ACTIVE";
    private String reason = "Customer approved";

    private CustomerStatusUpdateRequestBuilder() {
    }

    public static CustomerStatusUpdateRequestBuilder aStatusUpdate() {
        return new CustomerStatusUpdateRequestBuilder();
    }

    public CustomerStatusUpdateRequestBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public CustomerStatusUpdateRequestBuilder withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public CustomerStatusUpdateRequest build() {
        return new CustomerStatusUpdateRequest(status, reason);
    }
}

