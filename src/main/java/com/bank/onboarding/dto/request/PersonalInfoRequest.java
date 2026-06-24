package com.bank.onboarding.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Schema(description = "Personal information of the customer being onboarded")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfoRequest {

    @Schema(description = "Customer's legal first name", example = "John")
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Schema(description = "Customer's legal last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Schema(description = "Date of birth in ISO format", example = "1990-01-15")
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Schema(description = "ISO 3166-1 alpha-2 nationality code", example = "US")
    @NotBlank(message = "Nationality is required")
    @Size(min = 2, max = 3, message = "Nationality must be a 2 or 3 character ISO code")
    private String nationality;

    @Schema(description = "Tax identification number (SSN, EIN, etc.)", example = "123-45-6789")
    @Size(max = 50, message = "Tax identification number must not exceed 50 characters")
    private String taxIdentificationNumber;

    @Schema(description = "Primary email address", example = "john.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Schema(description = "Phone number in E.164 format", example = "+15550123456")
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Phone number must be in E.164 format")
    private String phoneNumber;
}

