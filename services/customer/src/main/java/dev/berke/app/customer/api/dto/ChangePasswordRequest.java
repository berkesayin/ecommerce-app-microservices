package dev.berke.app.customer.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank
        String oldPassword,

        // At least 8 chars, 1 digit, 1 lower, 1 upper, 1 special char
        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Password must be strong (8+ chars, digit, upper, lower, special)"
        )
        String newPassword
) {
}