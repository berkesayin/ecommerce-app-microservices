package dev.berke.app.auth.api.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String type,
        Long id,
        String customerId,
        String username,
        String email,
        List<String> roles
) {

    // custom constructor to set default value of "String type" = "Bearer"
    public LoginResponse(
            String token,
            Long id,
            String customerId,
            String username,
            String email,
            List<String> roles
    ) {
        this(token, "Bearer", id, customerId, username, email, roles);
    }
}
