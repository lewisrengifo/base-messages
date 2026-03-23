package com.lewisrp.basemessages.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User domain entity representing a user in the system.
 * This is a pure domain object with no framework dependencies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private Long id;
    private String email;
    private String passwordHash;
    private String name;
    private String avatarUrl;
    private boolean active;
    
    /**
     * Validates if the user can login (is active).
     */
    public boolean canLogin() {
        return active;
    }
    
    /**
     * Validates if the provided raw password matches the stored hash.
     * Note: The actual password validation will be done by the application layer
     * using the PasswordEncoderPort, this is just a placeholder method.
     */
    public boolean isPasswordValid(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.passwordHash);
    }
    
    /**
     * Port interface for password encoding (defined in domain to avoid dependencies).
     * The actual implementation will be provided by the infrastructure layer.
     */
    public interface PasswordEncoder {
        String encode(String rawPassword);
        boolean matches(String rawPassword, String encodedPassword);
    }
}
