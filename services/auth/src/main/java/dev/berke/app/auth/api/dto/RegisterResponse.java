package dev.berke.app.auth.api.dto;

public record RegisterResponse(
        Long id,
        String customerId,
        String username,
        String email
) {
}