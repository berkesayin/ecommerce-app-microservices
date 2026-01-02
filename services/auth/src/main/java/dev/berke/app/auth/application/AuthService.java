package dev.berke.app.auth.application;

import dev.berke.app.auth.infrastructure.client.customer.CustomerClient;
import dev.berke.app.auth.infrastructure.client.customer.CustomerCreateResponse;
import dev.berke.app.auth.infrastructure.client.customer.CustomerDataRequest;
import dev.berke.app.auth.api.dto.SignInRequest;
import dev.berke.app.auth.api.dto.SignInResponse;
import dev.berke.app.auth.api.dto.SignUpRequest;
import dev.berke.app.user.domain.model.Role;
import dev.berke.app.user.domain.model.RoleType;
import dev.berke.app.user.domain.model.User;
import dev.berke.app.user.domain.repository.RoleRepository;
import dev.berke.app.user.domain.repository.UserRepository;
import dev.berke.app.security.jwt.JwtUtils;
import dev.berke.app.security.service.UserDetailsImpl;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final ConcurrentHashMap<String, Boolean> tokenBlacklist = new ConcurrentHashMap<>();
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CustomerClient customerClient;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";

    public SignInResponse getToken(SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.username(),
                        signInRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new SignInResponse(
                jwt,
                userDetails.getId(),
                userDetails.getCustomerId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles
        );
    }

    public User createUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.username())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.email())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        CustomerDataRequest customerDataRequest = new CustomerDataRequest(
                signUpRequest.name(),
                signUpRequest.surname(),
                signUpRequest.gsmNumber(),
                signUpRequest.email()
        );

        String customerId = null;

        try {
            ResponseEntity<CustomerCreateResponse> customerResponse =
                    customerClient.createCustomer(customerDataRequest);

            if (customerResponse.getStatusCode().is2xxSuccessful() && customerResponse.getBody() != null) {
                customerId = customerResponse.getBody().customerId();
            } else {
                throw new RuntimeException("Error: Could not create customer! Status: "
                        + customerResponse.getStatusCode());
            }
        } catch (FeignException e) {
            throw new RuntimeException("Error creating customer via customer service: "
                    + e.getMessage(), e);
        }

        User user = new User(
                signUpRequest.username(),
                signUpRequest.email(),
                signUpRequest.name(),
                signUpRequest.surname(),
                signUpRequest.gsmNumber(),
                passwordEncoder.encode(signUpRequest.password())
        );
        user.setCustomerId(customerId);

        Set<String> strRoles = signUpRequest.role();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "backoffice":
                        Role backofficeRole = roleRepository.findByName(RoleType.ROLE_BACKOFFICE)
                                .orElseThrow(() -> new RuntimeException(
                                        "Error: Role is not found"));
                        roles.add(backofficeRole);
                        break;

                    default: // case "user" = customer
                        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException(
                                        "Error: Role is not found"));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);

        try {
           return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error: Could not save user registration data.", e);
        }
    }

    public void invalidateToken(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header.");
        }

        String token = tokenHeader.substring(7);

        if (token.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid Authorization header: Bearer token is empty.");
        }

        if (!jwtUtils.validateJwtToken(token)) {
            throw new IllegalArgumentException("Token is invalid or expired and cannot be blacklisted.");
        }

        long remainingMillis = jwtUtils.getRemainingValidityTimeFromToken(token);

        if (remainingMillis <= 0) {
            log.info("Token {} is already expired.", token);
            return;
        }

        try {
            Duration timeoutDuration = Duration.ofMillis(remainingMillis);
            redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + token,
                    "blacklisted",
                    timeoutDuration
            );
            log.info("Token blacklisted with TTL: {} ms", remainingMillis);
        } catch (Exception e) {
            log.error("Could not blacklist token in Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to blacklist token due to Redis error.", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
        } catch (Exception e) {
            log.error("Could not check token blacklist status in Redis: {}", e.getMessage(), e);
            return false;
        }
    }
}
