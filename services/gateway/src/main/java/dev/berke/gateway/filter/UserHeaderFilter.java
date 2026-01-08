package dev.berke.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .filter(c -> c.getAuthentication() != null)
                .flatMap(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

                    if (authentication.getPrincipal() instanceof Jwt) {
                        Jwt jwt = (Jwt) authentication.getPrincipal();

                        // extract customerId
                        String customerId = jwt.getClaimAsString("customerId");
                        if (customerId != null) {
                            requestBuilder.header("X-User-CustomerId", customerId);
                        } else {
                            log.warn("'customerId' claim not found in JWT.");
                        }

                        // roles/authorities (by JwtAuthenticationConverter)
                        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                        if (authorities != null && !authorities.isEmpty()) {
                            String rolesHeaderValue = authorities.stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.joining(","));
                            requestBuilder.header("X-User-Roles", rolesHeaderValue);
                        } else {
                            log.warn("No authorities found in Authentication object for JWT.");
                        }

                        // extract email
                        String email = jwt.getClaimAsString("email");
                        if (email != null) {
                            requestBuilder.header("X-User-Email", email);
                        } else {
                            log.warn("'email' claim not found in JWT.");
                        }

                        log.debug("Forwarding request to resource services with headers: " +
                                        "X-User-CustomerId={}, " +
                                        "X-User-Roles={}, " +
                                        "X-User-Email={}",
                                requestBuilder.build().getHeaders().getFirst("X-User-CustomerId"),
                                requestBuilder.build().getHeaders().getFirst("X-User-Roles"),
                                requestBuilder.build().getHeaders().getFirst("X-User-Email")
                        );

                    }

                    ServerHttpRequest newRequest = requestBuilder.build();
                    return chain.filter(exchange.mutate().request(newRequest).build());
                })
                .switchIfEmpty(chain.filter(exchange)); // public paths, no auth
    }

    @Override
    public int getOrder() {
        // runs after spring security filters (ReactiveSecurityContextHolder), before routing
        // and runs after TokenBlacklistFilter
        return -95;
    }
}