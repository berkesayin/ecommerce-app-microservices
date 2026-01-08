package dev.berke.app.basket.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BasketAddItemRequest(
        @NotNull(message = "Items list cannot be null")
        @NotEmpty(message = "Items list cannot be empty")
        @Valid
        List<BasketItemRequest> items
) {
}
