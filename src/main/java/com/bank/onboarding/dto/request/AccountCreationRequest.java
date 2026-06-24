package com.bank.onboarding.dto.request;

import com.bank.onboarding.domain.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Schema(description = "Request to open a new bank account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationRequest {

    @Schema(description = "Type of account to open", example = "CHECKING")
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-character ISO 4217 code")
    private String currency;

    @Schema(description = "Initial deposit amount (must be >= 0)", example = "1000.00")
    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.00", message = "Initial deposit must be non-negative")
    @Digits(integer = 15, fraction = 2, message = "Initial deposit must have at most 15 integer digits and 2 decimal places")
    private BigDecimal initialDeposit;

    @Schema(description = "Product code for specialized account products", example = "CHK-PREMIER")
    @Size(max = 20)
    private String productCode;
}

