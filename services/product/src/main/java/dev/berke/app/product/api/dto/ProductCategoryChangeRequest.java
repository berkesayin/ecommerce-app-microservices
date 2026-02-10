package dev.berke.app.product.api.dto;

import jakarta.validation.constraints.NotNull;

public record ProductCategoryChangeRequest(
        @NotNull(message = "Category ID is required")
        Integer newCategoryId
) {
}