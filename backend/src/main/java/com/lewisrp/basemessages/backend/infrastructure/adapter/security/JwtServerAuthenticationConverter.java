package com.lewisrp.basemessages.backend.infrastructure.adapter.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * Server authentication converter that extracts JWT tokens from the Authorization header.
 * Converts Bearer tokens into JwtAuthenticationToken objects for authentication.
 */
@Slf4j
@Component
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No valid Authorization header found");
            return Mono.empty();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        if (token.isEmpty()) {
            log.debug("Empty token in Authorization header");
            return Mono.empty();
        }

        log.debug("Extracted JWT token from Authorization header");
        // Return an unauthenticated token - the AuthenticationManager will validate it
        return Mono.just(new JwtAuthenticationToken(token, null, null, Collections.emptyList()));
    }
}
