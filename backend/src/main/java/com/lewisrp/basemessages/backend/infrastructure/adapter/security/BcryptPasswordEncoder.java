package com.lewisrp.basemessages.backend.infrastructure.adapter.security;

import com.lewisrp.basemessages.backend.application.port.outbound.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt Password Encoder implementation.
 * Uses Spring Security's BCryptPasswordEncoder.
 */
@Component
public class BcryptPasswordEncoder implements PasswordEncoderPort {
    
    private final BCryptPasswordEncoder encoder;
    
    public BcryptPasswordEncoder() {
        // Default strength is 10, which is a good balance between security and performance
        this.encoder = new BCryptPasswordEncoder();
    }
    
    @Override
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
