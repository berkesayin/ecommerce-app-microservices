package dev.berke.app.productsearch.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record ProductSearchRequest(
        String query,

        List<String> categories,

        List<String> manufacturers,

        @Valid
        PriceRange priceRange,

        SortCriteria sortBy,

        @Min(value = 0, message = "Page index must not be less than zero")
        int page,

        @Min(value = 1, message = "Page size must not be less than one")
        @Max(value = 200, message = "Page size must not exceed 200")
        int size
) {
    public record PriceRange(
            @Positive(message = "Min price must be positive")
            BigDecimal min,

            @Positive(message = "Max price must be positive")
            BigDecimal max
    ) {}

    public enum SortCriteria {
        RELEVANCE,
        PRICE_ASC,
        PRICE_DESC,
        NEWEST
    }
}