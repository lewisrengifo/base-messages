package com.lewisrp.basemessages.backend.infrastructure.adapter.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;

/**
 * Reactive authentication manager for JWT tokens.
 * Validates JWT access tokens and creates authenticated Authentication objects.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();

        if (token == null || token.isEmpty()) {
            log.debug("No token provided in authentication");
            return Mono.empty();
        }

        return jwtTokenProvider.validateAccessToken(token)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(valid -> {
                    if (!valid) {
                        log.debug("Invalid JWT token");
                        return Mono.empty();
                    }

                    // Extract user info from token
                    return jwtTokenProvider.extractEmailFromToken(token)
                            .zipWith(jwtTokenProvider.extractUserIdFromToken(token))
                            .map(tuple -> {
                                String email = tuple.getT1();
                                Long userId = tuple.getT2();

                                log.debug("Authenticated user: {}", email);

                                // Create authentication with user details
                                JwtAuthenticationToken authToken = new JwtAuthenticationToken(
                                        token,
                                        email,
                                        userId,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                                );
                                authToken.setAuthenticated(true);
                                return (Authentication) authToken;
                            });
                })
                .onErrorResume(e -> {
                    log.warn("Error validating JWT token: {}", e.getMessage());
                    return Mono.empty();
                });
    }
}
