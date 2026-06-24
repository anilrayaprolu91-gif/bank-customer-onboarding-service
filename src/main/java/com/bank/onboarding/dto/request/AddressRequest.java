package com.bank.onboarding.dto.request;

import com.bank.onboarding.domain.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Schema(description = "Address details for the customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @Schema(description = "Type of address", example = "HOME")
    @NotNull(message = "Address type is required")
    private AddressType addressType;

    @Schema(description = "Street address including house number", example = "123 Main Street, Apt 4B")
    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String street;

    @Schema(description = "City name", example = "New York")
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Schema(description = "State or province", example = "NY")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Schema(description = "Postal / ZIP code", example = "10001")
    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "US")
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 3, message = "Country must be a 2 or 3 character ISO code")
    private String country;

    @Schema(description = "Whether this is the primary address", example = "true")
    @NotNull(message = "isPrimary flag is required")
    private Boolean isPrimary;
}

