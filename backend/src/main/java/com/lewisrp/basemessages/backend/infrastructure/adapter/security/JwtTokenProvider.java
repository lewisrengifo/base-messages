package com.lewisrp.basemessages.backend.infrastructure.adapter.security;

import com.lewisrp.basemessages.backend.application.port.outbound.TokenProviderPort;
import com.lewisrp.basemessages.backend.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token Provider implementation using JJWT library.
 * Generates and validates JWT access tokens.
 */
@Slf4j
@Component
public class JwtTokenProvider implements TokenProviderPort {
    
    private static final long ACCESS_TOKEN_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000; // 7 days
    
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnlyChangeInProduction}")
    private String jwtSecret;
    
    private SecretKey secretKey;
    
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }
    
    @Override
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRY_MS);
        
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
    
    @Override
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRY_MS);
        
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
    
    @Override
    public Mono<Boolean> validateAccessToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = parseToken(token);
                return "access".equals(claims.get("type")) && !claims.getExpiration().before(new Date());
            } catch (Exception e) {
                log.warn("Invalid access token: {}", e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public Mono<String> extractEmailFromToken(String token) {
        return Mono.fromCallable(() -> {
            Claims claims = parseToken(token);
            return claims.getSubject();
        });
    }
    
    @Override
    public Mono<Long> extractUserIdFromToken(String token) {
        return Mono.fromCallable(() -> {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        });
    }
    
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
