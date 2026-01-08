package dev.berke.app.customer.api.dto;

public record CustomerSummaryResponse(
        String id,
        String name,
        String surname,
        String gsmNumber,
        String email
) {
}
