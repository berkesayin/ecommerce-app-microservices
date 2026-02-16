package dev.berke.app.product.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "Base price is required")
        @Positive(message = "Base price must be positive")
        BigDecimal basePrice,

        @NotNull(message = "Min price is required")
        @Positive(message = "Min price must be positive")
        BigDecimal minPrice
) {
}