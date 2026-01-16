package dev.berke.app.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

// client -> gateway -> auth
public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        Set<String> role,

        // At least 8 chars, 1 digit, 1 lower, 1 upper, 1 special char
        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Password must be strong (8+ chars, digit, upper, lower, special)"
        )
        String password,

        // email, name, surname, gsmNumber are sent to customer service
        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        String email,

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Surname is required")
        @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
        String surname,

        @NotBlank(message = "GSM number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid GSM number format")
        String gsmNumber
) {
}