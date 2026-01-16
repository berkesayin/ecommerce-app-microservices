package dev.berke.app.auth.api;

import dev.berke.app.auth.api.dto.LoginRequest;
import dev.berke.app.auth.api.dto.LoginResponse;
import dev.berke.app.auth.api.dto.RegisterRequest;
import dev.berke.app.auth.api.dto.RegisterResponse;
import dev.berke.app.auth.application.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(
            @RequestBody @Valid LoginRequest loginRequest
    ) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(
            @RequestBody @Valid RegisterRequest registerRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerUser(registerRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        authService.logout(tokenHeader);
        return ResponseEntity.noContent().build();
    }
}