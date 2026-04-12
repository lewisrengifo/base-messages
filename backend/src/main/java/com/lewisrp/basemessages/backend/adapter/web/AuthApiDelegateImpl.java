package com.lewisrp.basemessages.backend.adapter.web;

import com.lewisrp.basemessages.backend.application.port.inbound.AuthUseCase;
import com.lewisrp.basemessages.backend.adapter.web.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.AuthApiDelegate;
import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Implementation of the OpenAPI generated AuthApiDelegate interface.
 * This is the web adapter that bridges the HTTP layer with the application layer.
 * Uses MapStruct for DTO conversions.
 */
@Component
@RequiredArgsConstructor
public class AuthApiDelegateImpl implements AuthApiDelegate {

    private final AuthUseCase authUseCase;
    private final AuthMapper authMapper;

    @Override
    public Mono<ResponseEntity<LoginResponse>> authLoginPost(Mono<LoginRequest> loginRequest, ServerWebExchange exchange) {
        return loginRequest
                .map(authMapper::toLoginCommand)
                .flatMap(authUseCase::login)
                .map(authMapper::toLoginResponse)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> authLogoutPost(ServerWebExchange exchange) {
        // For logout, we would typically get the refresh token from the request
        // For now, we'll just return 204 as the API specification requires
        return Mono.just(ResponseEntity.noContent().build());
    }
}
