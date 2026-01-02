package dev.berke.app.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Slf4j
public class FeignClientInterceptor implements RequestInterceptor {

    // headers to send
    private static final String HEADER_CUSTOMER_ID = "X-User-CustomerId";
    private static final String HEADER_ROLES = "X-User-Roles";
    private static final String HEADER_EMAIL = "X-User-Email";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 1. send authorization bearer
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                template.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }

            // 2. send custom identity headers
            String customerId = request.getHeader(HEADER_CUSTOMER_ID);
            if (customerId != null) {
                template.header(HEADER_CUSTOMER_ID, customerId);
            }

            String roles = request.getHeader(HEADER_ROLES);
            if (roles != null) {
                template.header(HEADER_ROLES, roles);
            }

            String email = request.getHeader(HEADER_EMAIL);
            if (email != null) {
                template.header(HEADER_EMAIL, email);
            }

            log.info("Sent headers for Feign Call to {}: Auth={}, CustomerId={}",
                    template.feignTarget().url() + template.url(),
                    (authorizationHeader != null ? "auth header exists" : "does not exist"),
                    customerId);
        }
    }
}