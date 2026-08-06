package com.bank.onboarding.accounts.api;

import com.bank.onboarding.dto.request.AccountCreationRequest;
import com.bank.onboarding.dto.response.AccountResponse;
import com.bank.onboarding.dto.response.ApiResponse;
import com.bank.onboarding.accounts.application.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Management", description = "APIs for managing customer bank accounts")
public class AccountController {

    private final AccountService accountService;

    // â”€â”€ POST /api/v1/customers/{customerId}/accounts â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/customers/{customerId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary     = "Open a new account",
        description = "Opens a new bank account for an existing ACTIVE customer"
    )
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Parameter(description = "Customer UUID") @PathVariable UUID customerId,
            @Valid @RequestBody AccountCreationRequest request) {

        log.info("POST /api/v1/customers/{}/accounts â€“ type={}", customerId, request.getAccountType());
        AccountResponse response = accountService.createAccount(customerId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Account created successfully"));
    }

    // â”€â”€ GET /api/v1/customers/{customerId}/accounts â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/customers/{customerId}/accounts")
    @Operation(summary = "List accounts for a customer", description = "Returns all accounts belonging to a customer")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomer(
            @PathVariable UUID customerId) {

        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(accounts, "Accounts retrieved successfully"));
    }

    // â”€â”€ GET /api/v1/accounts/{accountId} â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "Get account by ID", description = "Returns a single account by UUID")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @PathVariable UUID accountId) {

        AccountResponse response = accountService.getAccountById(accountId);
        return ResponseEntity.ok(ApiResponse.success(response, "Account retrieved successfully"));
    }

    // â”€â”€ DELETE /api/v1/accounts/{accountId} â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @DeleteMapping("/accounts/{accountId}")
    @Operation(summary = "Close an account", description = "Closes an active bank account")
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(
            @PathVariable UUID accountId) {

        AccountResponse response = accountService.closeAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success(response, "Account closed successfully"));
    }
}



