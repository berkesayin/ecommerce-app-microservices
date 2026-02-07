package dev.berke.app.customer.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record CustomerUpdateResponse(
        String id,
        String name,
        String surname,
        String email,
        String gsmNumber,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant updatedAt
) {
}