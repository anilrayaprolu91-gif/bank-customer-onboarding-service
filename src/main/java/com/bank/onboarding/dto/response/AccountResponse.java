package com.bank.onboarding.dto.response;

import com.bank.onboarding.domain.enums.AccountStatus;
import com.bank.onboarding.domain.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Bank account linked to a customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    @Schema(description = "Unique account identifier")
    private UUID id;

    @Schema(description = "Bank account number", example = "ACC-0001-20241101-001")
    private String accountNumber;

    @Schema(description = "Customer identifier this account belongs to")
    private UUID customerId;

    @Schema(description = "Account type", example = "CHECKING")
    private AccountType accountType;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    private String currency;

    @Schema(description = "Current account balance", example = "1000.00")
    private BigDecimal balance;

    @Schema(description = "Current account status", example = "ACTIVE")
    private AccountStatus accountStatus;

    @Schema(description = "Account opening date")
    private LocalDateTime openedDate;

    @Schema(description = "Account closure date (null if active)")
    private LocalDateTime closedDate;

    @Schema(description = "Product code", example = "CHK-PREMIER")
    private String productCode;

    @Schema(description = "Annual interest rate", example = "0.0350")
    private BigDecimal interestRate;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last-updated timestamp")
    private LocalDateTime updatedAt;
}

