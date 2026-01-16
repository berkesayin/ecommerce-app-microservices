package dev.berke.app.customer.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerUpdateRequest(
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
        String surname,

        // length 10-15
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid GSM number format")
        String gsmNumber,

        @Email(message = "Email is not valid")
        String email,

        // At least 8 chars, 1 digit, 1 lower, 1 upper, 1 special char
        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Password must be strong (8+ chars, digit, upper, lower, special)"
        )
        String password,

        @Pattern(regexp = "^[0-9]{11}$", message = "Identity number must be 11 digits")
        String identityNumber,

        @Size(max = 200, message = "Address cannot exceed 200 characters")
        String registrationAddress
) {
}