package com.lewisrp.basemessages.backend.application.port.outbound;

/**
 * Outbound port for password encoding operations.
 * This interface abstracts the password hashing mechanism.
 */
public interface PasswordEncoderPort {
    
    /**
     * Encode a raw password using BCrypt.
     * 
     * @param rawPassword the raw password to encode
     * @return the encoded password
     */
    String encode(String rawPassword);
    
    /**
     * Check if a raw password matches an encoded password.
     * 
     * @param rawPassword the raw password to check
     * @param encodedPassword the encoded password to compare against
     * @return true if they match, false otherwise
     */
    boolean matches(String rawPassword, String encodedPassword);
}
