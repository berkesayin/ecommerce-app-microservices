package dev.berke.app.customer.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerDataRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Surname is required")
        @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
        String surname,

        // length 10-15
        @NotBlank(message = "GSM number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid GSM number format")
        String gsmNumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        String email
) {
}