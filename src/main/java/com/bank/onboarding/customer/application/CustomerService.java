package com.bank.onboarding.customer.application;

import com.bank.onboarding.dto.request.CustomerStatusUpdateRequest;
import com.bank.onboarding.dto.response.CustomerResponse;
import com.bank.onboarding.dto.response.OnboardingStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    /**
     * Retrieves a full customer profile including all related sub-entities.
     */
    CustomerResponse getCustomerById(UUID id);

    /**
     * Retrieves a full customer profile by their bank-assigned customer number.
     */
    CustomerResponse getCustomerByCustomerNumber(String customerNumber);

    /**
     * Returns a paginated list of all customers.
     */
    Page<CustomerResponse> getAllCustomers(Pageable pageable);

    /**
     * Updates the onboarding / account status of a customer.
     */
    CustomerResponse updateCustomerStatus(UUID id, CustomerStatusUpdateRequest request);

    /**
     * Returns a lightweight status summary for a customer.
     */
    OnboardingStatusResponse getOnboardingStatus(UUID id);
}
