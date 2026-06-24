package com.bank.onboarding.dto.response;

import com.bank.onboarding.domain.enums.AddressType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Address associated with a customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressResponse {

    @Schema(description = "Unique address identifier")
    private UUID id;

    @Schema(description = "Type of address", example = "HOME")
    private AddressType addressType;

    @Schema(description = "Street address", example = "123 Main Street, Apt 4B")
    private String street;

    @Schema(description = "City", example = "New York")
    private String city;

    @Schema(description = "State or province", example = "NY")
    private String state;

    @Schema(description = "Postal / ZIP code", example = "10001")
    private String postalCode;

    @Schema(description = "ISO country code", example = "US")
    private String country;

    @Schema(description = "Whether this is the primary address", example = "true")
    private Boolean isPrimary;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last-updated timestamp")
    private LocalDateTime updatedAt;
}

