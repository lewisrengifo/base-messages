package com.lewisrp.basemessages.backend.application.port.outbound;

import com.lewisrp.basemessages.backend.domain.model.User;
import reactor.core.publisher.Mono;

/**
 * Outbound port for JWT token operations.
 * This interface abstracts the token generation and validation mechanism.
 */
public interface TokenProviderPort {
    
    /**
     * Generate an access token for a user.
     * Short-lived (15 minutes).
     * 
     * @param user the user to generate token for
     * @return the generated access token
     */
    String generateAccessToken(User user);
    
    /**
     * Generate a refresh token for a user.
     * Long-lived (7-30 days).
     * 
     * @param user the user to generate token for
     * @return the generated refresh token
     */
    String generateRefreshToken(User user);
    
    /**
     * Validate an access token.
     * 
     * @param token the token to validate
     * @return Mono emitting true if valid, false otherwise
     */
    Mono<Boolean> validateAccessToken(String token);
    
    /**
     * Extract user email from a token.
     * 
     * @param token the token to extract from
     * @return Mono emitting the email
     */
    Mono<String> extractEmailFromToken(String token);
    
    /**
     * Extract user ID from a token.
     * 
     * @param token the token to extract from
     * @return Mono emitting the user ID
     */
    Mono<Long> extractUserIdFromToken(String token);
}
