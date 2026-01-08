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

// @Component
@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_CUSTOMER_ID = "X-User-CustomerId";
    public static final String HEADER_ROLES = "X-User-Roles";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String HEADER_EMAIL = "X-User-Email";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String customerId = request.getHeader(HEADER_CUSTOMER_ID);
        String rolesHeader = request.getHeader(HEADER_ROLES);
        String email = request.getHeader(HEADER_EMAIL); // we have customerId, do not need this

        if (StringUtils.hasText(customerId) && StringUtils.hasText(rolesHeader)) {
            log.debug(
                    "Headers received: {}='{}', {}='{}', {}='{}'",
                    HEADER_CUSTOMER_ID, customerId,
                    HEADER_ROLES, rolesHeader,
                    HEADER_EMAIL, email
            );

            List<GrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(roleStr -> !roleStr.isEmpty())
                    .map(role -> {
                        if (!role.startsWith(ROLE_PREFIX)) {
                            return new SimpleGrantedAuthority(ROLE_PREFIX + role);
                        }
                        return new SimpleGrantedAuthority(role);
                    })
                    .collect(Collectors.toList());

            // principal is customerId
            // @AuthenticationPrincipal String principal => principal = customerId
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(customerId, null, authorities);

            Map<String, String> details = new HashMap<>();
            details.put("customerId", customerId); // also explicitly adding customerId
            if (StringUtils.hasText(email)) {
                details.put("email", email);
            }
            authentication.setDetails(details);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("" + "SecurityContext provides principal (customerId): '{}', " +
                    "roles: {}, and email: {}", customerId, authorities, details.containsKey("email")
            );

        } else {
            if (!StringUtils.hasText(customerId)) {
                log.warn("{} header is missing or empty. Cannot authenticate.", HEADER_CUSTOMER_ID);
            }
            if (!StringUtils.hasText(rolesHeader)) {
                log.warn("{} header is missing or empty. Cannot authenticate.", HEADER_ROLES);
            }
        }

        filterChain.doFilter(request, response);
    }
}