package dev.berke.app.customer.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateContactRequest(
        // length 10-15
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid GSM number format")
        String gsmNumber,

        @Email(message = "Email is not valid")
        String email,

        @Size(max = 200, message = "Address cannot exceed 200 characters")
        String registrationAddress
) {
}