package dev.berke.app.shared.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    // header names are same with gateway's UserHeaderFilter values
    public static final String HEADER_CUSTOMER_ID = "X-User-CustomerId";
    public static final String HEADER_ROLES = "X-User-Roles";
    public static final String HEADER_EMAIL = "X-User-Email";
    public static final String ROLE_PREFIX = "ROLE_";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String customerId = request.getHeader(HEADER_CUSTOMER_ID);
        String rolesHeader = request.getHeader(HEADER_ROLES);
        String email = request.getHeader(HEADER_EMAIL);

        log.debug("OrderService - Request to {}: {} received with headers: " +
                        "customerID='{}', roles='{}', email='{}'",
                request.getMethod(), request.getRequestURI(), customerId, rolesHeader, email);

        if (StringUtils.hasText(customerId) && StringUtils.hasText(rolesHeader)) {
            List<GrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(roleStr -> !roleStr.isEmpty())
                    .map(role -> {
                        String upperRole = role.toUpperCase();

                        if (!upperRole.startsWith(ROLE_PREFIX) && !upperRole.startsWith("SCOPE_")) {
                            return new SimpleGrantedAuthority(ROLE_PREFIX + upperRole);
                        }

                        return new SimpleGrantedAuthority(upperRole);
                    })
                    .collect(Collectors.toList());

            // the principal here is the customerId
            // in controller, we can get it via @AuthenticationPrincipal String principal
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(customerId, null, authorities);

            Map<String, String> details = new HashMap<>();

            details.put("customerId", customerId);

            if (StringUtils.hasText(email)) {
                details.put("email", email);
            }

            authentication.setDetails(details);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("OrderService - for customerId: '{}', roles: {}, details: {}",
                    customerId, authorities, details
            );
        } else {
            log.warn("PaymentService - {} or {} header is missing or empty. " ,
                    HEADER_CUSTOMER_ID, HEADER_ROLES, request.getRequestURI()
            );
        }

        filterChain.doFilter(request, response);
    }
}