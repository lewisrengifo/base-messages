package com.lewisrp.basemessages.backend.infrastructure.adapter.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Authentication token for JWT-based authentication.
 * Holds the JWT token and user information extracted from it.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private final String email;
    private final Long userId;

    public JwtAuthenticationToken(String token, String email, Long userId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.email = email;
        this.userId = userId;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }
}
