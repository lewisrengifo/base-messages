package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.repository;

import com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity.TemplateEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for TemplateEntity.
 */
@Repository
public interface TemplateR2dbcRepository extends ReactiveCrudRepository<TemplateEntity, Long> {

    /**
     * Find all templates with pagination.
     */
    Flux<TemplateEntity> findAllBy(Pageable pageable);

    /**
     * Find templates by status.
     */
    Flux<TemplateEntity> findByStatus(String status, Pageable pageable);

    /**
     * Find templates by category.
     */
    Flux<TemplateEntity> findByCategory(String category, Pageable pageable);

    /**
     * Find templates by status and category.
     */
    Flux<TemplateEntity> findByStatusAndCategory(String status, String category, Pageable pageable);

    /**
     * Search templates by name or content (case insensitive).
     */
    Flux<TemplateEntity> findByNameContainingIgnoreCaseOrContentContainingIgnoreCase(
            String name, String content, Pageable pageable);

    /**
     * Check if a template with the given name exists.
     */
    Mono<Boolean> existsByName(String name);

    /**
     * Count templates by status.
     */
    Mono<Long> countByStatus(String status);

    /**
     * Save template with explicit enum casting for PostgreSQL.
     */
    @Query("INSERT INTO templates (name, category, language, status, content, created_at, updated_at) " +
           "VALUES (:#{#entity.name}, CAST(:#{#entity.category} AS template_category), :#{#entity.language}, " +
           "CAST(:#{#entity.status} AS template_status), :#{#entity.content}, :#{#entity.createdAt}, :#{#entity.updatedAt}) " +
           "RETURNING *")
    Mono<TemplateEntity> saveWithEnumCast(TemplateEntity entity);
}
