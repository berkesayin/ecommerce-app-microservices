package dev.berke.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer; // custom response body

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class TokenBlacklistFilter implements GlobalFilter, Ordered {

    // same as AuthService
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private final ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    public TokenBlacklistFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    if (authentication != null &&
                            authentication.isAuthenticated() &&
                            authentication.getPrincipal() instanceof Jwt
                    ) {
                        Jwt jwt = (Jwt) authentication.getPrincipal();
                        String tokenValue = jwt.getTokenValue();
                        String redisKey = TOKEN_BLACKLIST_PREFIX + tokenValue;

                        return redisTemplate.hasKey(redisKey)
                                .flatMap(isBlacklisted -> {
                                    if (Boolean.TRUE.equals(isBlacklisted)) {
                                        log.warn("Access denied. Token is blacklisted: {}",
                                                maskToken(tokenValue));

                                        return sendErrorResponse(
                                                exchange,
                                                HttpStatus.UNAUTHORIZED,
                                                "token_blacklisted",
                                                "Your session has been invalidated. Please login again."
                                        );
                                    }
                                    return chain.filter(exchange); // not blacklisted
                                })
                                .onErrorResume(throwable -> {
                                    log.error("Redis error while checking token blacklist.", throwable);
                                    return sendErrorResponse(
                                            exchange,
                                            HttpStatus.SERVICE_UNAVAILABLE,
                                            "redis_error",
                                            "Error validating session. Please try again later."
                                    );
                                });
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Void> sendErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String errorCode,
            String errorMessage
    ) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorBody = String.format("{\"error\":\"%s\", \"message\":\"%s\"}", errorCode, errorMessage);
        byte[] bytes = errorBody.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "TOKEN_TOO_SHORT";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }

    @Override
    public int getOrder() {
        // after Spring Security authentication (-100), before UserHeaderFilter.
        return -99;
    }
}