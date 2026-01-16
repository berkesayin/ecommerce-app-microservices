package dev.berke.app.auth.application.mapper;

import dev.berke.app.auth.api.dto.RegisterResponse;
import dev.berke.app.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    public RegisterResponse toUserRegisterResponse(User user) {
        if (user == null) {
            return null;
        }

        return new RegisterResponse(
                user.getId(),
                user.getCustomerId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}