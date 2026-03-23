package com.lewisrp.basemessages.backend.application.port.inbound;

import com.lewisrp.basemessages.backend.application.dto.LoginCommand;
import com.lewisrp.basemessages.backend.application.dto.LoginResult;
import reactor.core.publisher.Mono;

/**
 * Inbound port for authentication use cases.
 * This interface defines the contract that the application layer exposes to the infrastructure layer.
 */
public interface AuthUseCase {
    
    /**
     * Authenticate a user with email and password.
     * Returns access and refresh tokens on success.
     * 
     * @param command login credentials
     * @return Mono emitting login result with tokens
     */
    Mono<LoginResult> login(LoginCommand command);
    
    /**
     * Logout a user by invalidating their refresh token.
     * 
     * @param refreshToken the refresh token to invalidate
     * @return Mono completing when logout is done
     */
    Mono<Void> logout(String refreshToken);
}
