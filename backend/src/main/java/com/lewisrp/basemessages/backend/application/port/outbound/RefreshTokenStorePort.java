package com.lewisrp.basemessages.backend.application.port.outbound;

/**
 * Outbound port for refresh token storage and management.
 * Refresh tokens are stored server-side to enable revocation.
 */
public interface RefreshTokenStorePort {
    
    /**
     * Store a refresh token associated with a user.
     * 
     * @param token the refresh token
     * @param userId the user ID associated with the token
     * @param expirySeconds token expiry time in seconds
     */
    void store(String token, Long userId, long expirySeconds);
    
    /**
     * Validate a refresh token and retrieve the associated user ID.
     * 
     * @param token the refresh token to validate
     * @return the user ID if valid and not expired, null otherwise
     */
    Long validateAndGetUserId(String token);
    
    /**
     * Revoke a refresh token.
     * 
     * @param token the token to revoke
     */
    void revoke(String token);
    
    /**
     * Revoke all refresh tokens for a user.
     * 
     * @param userId the user ID
     */
    void revokeAllForUser(Long userId);
}
