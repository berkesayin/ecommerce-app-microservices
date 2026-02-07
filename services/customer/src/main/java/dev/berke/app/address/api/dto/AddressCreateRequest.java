package dev.berke.app.address.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressCreateRequest(
        @NotBlank(message = "Contact name is required")
        String contactName,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Address is required")
        String addressLine,

        String zipCode
) {
}