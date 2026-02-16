package dev.berke.app.customer.api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateIdentityRequest(
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
        String surname,

        @Pattern(regexp = "^[0-9]{11}$", message = "Identity number must be 11 digits")
        String identityNumber
) {
}