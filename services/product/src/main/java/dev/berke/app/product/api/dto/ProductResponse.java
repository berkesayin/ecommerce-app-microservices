package dev.berke.app.product.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Integer productId,
        String productName,
        BigDecimal basePrice,
        BigDecimal minPrice,
        String manufacturer,
        String sku,

        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdOn,
        Boolean status,

        Integer categoryId
) {
}
