package com.bank.onboarding.controller;

import com.bank.onboarding.dto.request.CustomerOnboardingRequest;
import com.bank.onboarding.dto.request.CustomerStatusUpdateRequest;
import com.bank.onboarding.dto.response.ApiResponse;
import com.bank.onboarding.dto.response.CustomerResponse;
import com.bank.onboarding.dto.response.OnboardingStatusResponse;
import com.bank.onboarding.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Onboarding", description = "APIs for managing the full customer onboarding lifecycle")
public class CustomerController {

    private final CustomerService customerService;

    // ── POST /api/v1/customers/onboard ────────────────────────────────────────

    @PostMapping("/onboard")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary     = "Onboard a new customer",
        description = "Creates a full customer profile with addresses, KYC documents, risk profile " +
                      "and an optional initial bank account in a single atomic operation."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Customer onboarded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Customer already exists"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> onboardCustomer(
            @Valid @RequestBody CustomerOnboardingRequest request) {

        log.info("POST /api/v1/customers/onboard – email={}", request.getPersonalInfo().getEmail());
        CustomerResponse response = customerService.onboardCustomer(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Customer onboarded successfully"));
    }

    // ── GET /api/v1/customers ─────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all customers", description = "Returns a paginated list of all customers")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getAllCustomers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<CustomerResponse> page = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(ApiResponse.success(page, "Customers retrieved successfully"));
    }

    // ── GET /api/v1/customers/{id} ────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Returns full customer profile by UUID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @Parameter(description = "Customer UUID") @PathVariable UUID id) {

        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer retrieved successfully"));
    }

    // ── GET /api/v1/customers/number/{customerNumber} ─────────────────────────

    @GetMapping("/number/{customerNumber}")
    @Operation(summary = "Get customer by customer number",
               description = "Returns full customer profile by bank-assigned customer number")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByNumber(
            @Parameter(description = "Bank-assigned customer number, e.g. CUST-0001-20241101")
            @PathVariable String customerNumber) {

        CustomerResponse response = customerService.getCustomerByCustomerNumber(customerNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer retrieved successfully"));
    }

    // ── PATCH /api/v1/customers/{id}/status ───────────────────────────────────

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update customer status",
               description = "Updates the onboarding / account status of a customer (e.g. ACTIVE, SUSPENDED)")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusUpdateRequest request) {

        CustomerResponse response = customerService.updateCustomerStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer status updated successfully"));
    }

    // ── GET /api/v1/customers/{id}/onboarding-status ──────────────────────────

    @GetMapping("/{id}/onboarding-status")
    @Operation(summary = "Get onboarding status summary",
               description = "Returns a lightweight summary of the customer's onboarding progress")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> getOnboardingStatus(
            @PathVariable UUID id) {

        OnboardingStatusResponse response = customerService.getOnboardingStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Onboarding status retrieved successfully"));
    }
}

