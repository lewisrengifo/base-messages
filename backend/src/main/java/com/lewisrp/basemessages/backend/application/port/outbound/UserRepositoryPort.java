package com.lewisrp.basemessages.backend.application.port.outbound;

import com.lewisrp.basemessages.backend.domain.model.User;
import reactor.core.publisher.Mono;

/**
 * Outbound port for user persistence operations.
 * This interface abstracts the persistence mechanism from the application layer.
 */
public interface UserRepositoryPort {
    
    /**
     * Find a user by their email address.
     * 
     * @param email the email to search for
     * @return Mono emitting the user if found, empty otherwise
     */
    Mono<User> findByEmail(String email);
    
    /**
     * Find a user by their ID.
     * 
     * @param id the user ID
     * @return Mono emitting the user if found, empty otherwise
     */
    Mono<User> findById(Long id);

    /**
     * Save a user entity.
     * 
     * @param user the user to save
     * @return Mono emitting the saved user
     */
    Mono<User> save(User user);

    /**
     * Check if a user with the given email exists.
     * 
     * @param email the email to check
     * @return Mono emitting true if exists, false otherwise
     */
    Mono<Boolean> existsByEmail(String email);
}
