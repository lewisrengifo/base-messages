package com.lewisrp.basemessages.backend.adapter.persistence.repository;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.ContactEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for ContactEntity.
 */
@Repository
public interface ContactR2dbcRepository extends ReactiveCrudRepository<ContactEntity, Long> {

    Flux<ContactEntity> findAllByUserId(Long userId, Pageable pageable);

    Flux<ContactEntity> findByUserIdAndNameContainingIgnoreCase(
            Long userId,
            String query,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT *
            FROM contacts
            WHERE user_id = :userId
              AND (
                LOWER(name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR phone LIKE CONCAT('%', :query, '%')
              )
            ORDER BY id DESC
            LIMIT :#{#pageable.pageSize}
            OFFSET :#{#pageable.offset}
            """)
    Flux<ContactEntity> searchByUserId(Long userId, String query, Pageable pageable);

    Mono<Boolean> existsByUserIdAndPhone(Long userId, String phone);

    Mono<Long> countByUserId(Long userId);

    @Query("""
            SELECT COUNT(DISTINCT id)
            FROM contacts
            WHERE user_id = :userId
              AND (
                LOWER(name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR phone LIKE CONCAT('%', :query, '%')
              )
            """)
    Mono<Long> countSearchByUserId(Long userId, String query);
}
