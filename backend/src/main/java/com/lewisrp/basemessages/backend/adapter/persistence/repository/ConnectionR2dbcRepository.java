package com.lewisrp.basemessages.backend.adapter.persistence.repository;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.ConnectionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for ConnectionEntity.
 */
@Repository
public interface ConnectionR2dbcRepository extends ReactiveCrudRepository<ConnectionEntity, Long> {

    /**
     * Find the active connection (we only support one connection at a time for MVP).
     */
    Mono<ConnectionEntity> findFirstByOrderByIdDesc();

    /**
     * Check if any connection exists.
     */
    Mono<Boolean> existsBy();

    /**
     * Find connection by status.
     */
    Mono<ConnectionEntity> findFirstByStatus(String status);
}
