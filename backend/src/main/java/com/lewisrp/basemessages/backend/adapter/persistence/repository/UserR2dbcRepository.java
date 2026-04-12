package com.lewisrp.basemessages.backend.adapter.persistence.repository;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for UserEntity.
 */
@Repository
public interface UserR2dbcRepository extends ReactiveCrudRepository<UserEntity, Long> {
    
    /**
     * Find a user by their email address.
     * 
     * @param email the email to search for
     * @return Mono emitting the user if found, empty otherwise
     */
    Mono<UserEntity> findByEmail(String email);
    
    /**
     * Check if a user with the given email exists.
     * 
     * @param email the email to check
     * @return Mono emitting true if exists, false otherwise
     */
    Mono<Boolean> existsByEmail(String email);
}
