package com.lewisrp.basemessages.backend.infrastructure.adapter.security;

import com.lewisrp.basemessages.backend.application.port.outbound.RefreshTokenStorePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory refresh token store implementation.
 * Stores refresh tokens server-side to enable revocation.
 * 
 * Note: For production, consider using Redis or database storage for distributed systems.
 */
@Slf4j
@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStorePort {
    
    private final Map<String, RefreshTokenData> tokenStore = new ConcurrentHashMap<>();
    
    @Override
    public void store(String token, Long userId, long expirySeconds) {
        RefreshTokenData data = new RefreshTokenData(userId, Instant.now().plusSeconds(expirySeconds));
        tokenStore.put(token, data);
        log.debug("Stored refresh token for user: {}, expires in {} seconds", userId, expirySeconds);
    }
    
    @Override
    public Long validateAndGetUserId(String token) {
        RefreshTokenData data = tokenStore.get(token);
        
        if (data == null) {
            log.warn("Refresh token not found");
            return null;
        }
        
        if (data.isExpired()) {
            log.warn("Refresh token expired for user: {}", data.getUserId());
            tokenStore.remove(token);
            return null;
        }
        
        log.debug("Refresh token validated for user: {}", data.getUserId());
        return data.getUserId();
    }
    
    @Override
    public void revoke(String token) {
        RefreshTokenData removed = tokenStore.remove(token);
        if (removed != null) {
            log.info("Refresh token revoked for user: {}", removed.getUserId());
        }
    }
    
    @Override
    public void revokeAllForUser(Long userId) {
        tokenStore.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId));
        log.info("All refresh tokens revoked for user: {}", userId);
    }
    
    /**
     * Inner class to hold refresh token metadata.
     */
    private static class RefreshTokenData {
        private final Long userId;
        private final Instant expiryTime;
        
        public RefreshTokenData(Long userId, Instant expiryTime) {
            this.userId = userId;
            this.expiryTime = expiryTime;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }
    }
}
