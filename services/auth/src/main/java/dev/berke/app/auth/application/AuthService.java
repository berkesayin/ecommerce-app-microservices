package dev.berke.app.auth.application;

import dev.berke.app.auth.api.dto.RegisterRequest;
import dev.berke.app.auth.api.dto.LoginRequest;
import dev.berke.app.auth.api.dto.LoginResponse;
import dev.berke.app.auth.api.dto.RegisterResponse;
import dev.berke.app.auth.application.mapper.AuthMapper;
import dev.berke.app.auth.infrastructure.client.customer.CustomerClient;
import dev.berke.app.auth.infrastructure.client.customer.CustomerDataRequest;
import dev.berke.app.security.jwt.JwtUtils;
import dev.berke.app.security.service.UserDetailsImpl;
import dev.berke.app.shared.exception.CustomerServiceCommunicationException;
import dev.berke.app.shared.exception.EmailAlreadyExistsException;
import dev.berke.app.shared.exception.InvalidTokenException;
import dev.berke.app.shared.exception.RoleNotFoundException;
import dev.berke.app.shared.exception.UsernameAlreadyExistsException;
import dev.berke.app.user.domain.model.Role;
import dev.berke.app.user.domain.model.RoleType;
import dev.berke.app.user.domain.model.User;
import dev.berke.app.user.domain.repository.RoleRepository;
import dev.berke.app.user.domain.repository.UserRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CustomerClient customerClient;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private static final String BEARER_PREFIX = "Bearer ";

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getCustomerId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles
        );
    }

    @Transactional
    public RegisterResponse registerUser(RegisterRequest registerRequest) {
        validateUserUniqueness(registerRequest);

        String customerId = createRemoteCustomer(registerRequest);
        Set<Role> roles = resolveRoles(registerRequest.role());

        User user = new User(
                registerRequest.username(),
                registerRequest.email(),
                registerRequest.name(),
                registerRequest.surname(),
                registerRequest.gsmNumber(),
                passwordEncoder.encode(registerRequest.password())
        );

        user.setCustomerId(customerId);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return authMapper.toUserRegisterResponse(savedUser);
    }

    public void logout(String tokenHeader) {
        String token = extractToken(tokenHeader);
        validateTokenForLogout(token);

        long remainingMillis = jwtUtils.getRemainingValidityTimeFromToken(token);

        if (remainingMillis > 0) {
            blacklistToken(token, remainingMillis);
        } else {
            log.info("Token was already expired, skipping blacklist.");
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }

    private void validateUserUniqueness(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new UsernameAlreadyExistsException(
                    String.format("Error: Username '%s' is already taken!",
                            registerRequest.username())
            );
        }

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new EmailAlreadyExistsException(
                    String.format("Error: Email '%s' is already in use!",
                            registerRequest.email())
            );
        }
    }

    private String createRemoteCustomer(RegisterRequest request) {
        CustomerDataRequest customerDataRequest = new CustomerDataRequest(
                request.name(),
                request.surname(),
                request.gsmNumber(),
                request.email()
        );

        try {
            var response = customerClient.createCustomer(customerDataRequest);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().customerId();
            }

            throw new CustomerServiceCommunicationException("Failed to create customer. Status: "
                    + response.getStatusCode(), null);
        } catch (FeignException e) {
            throw new CustomerServiceCommunicationException("Error calling Customer Service", e);
        }
    }

    private Set<Role> resolveRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(getRoleByType(RoleType.ROLE_USER));
            return roles;
        }

        strRoles.forEach(role -> {
            switch (role.toLowerCase()) {
                case "backoffice" -> roles.add(getRoleByType(RoleType.ROLE_BACKOFFICE));
                default -> roles.add(getRoleByType(RoleType.ROLE_USER));
            }
        });

        return roles;
    }

    private Role getRoleByType(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseThrow(() -> new RoleNotFoundException(
                        String.format("Error: Role '%s' is not found.", roleType.name())
                ));
    }

    private String extractToken(String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith(BEARER_PREFIX)) {
            return tokenHeader.substring(7);
        }

        throw new InvalidTokenException("Missing or invalid Authorization header");
    }

    private void validateTokenForLogout(String token) {
        if (!jwtUtils.validateJwtToken(token)) {
            throw new InvalidTokenException("Token is invalid or expired.");
        }
    }

    private void blacklistToken(String token, long ttlMillis) {
        try {
            redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + token,
                    "blacklisted",
                    Duration.ofMillis(ttlMillis)
            );
            log.info("Token blacklisted with TTL: {} ms", ttlMillis);
        } catch (Exception e) {
            log.error("Redis error during blacklist", e);
        }
    }
}