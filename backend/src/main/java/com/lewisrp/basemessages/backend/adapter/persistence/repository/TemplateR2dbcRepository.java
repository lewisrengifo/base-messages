package com.lewisrp.basemessages.backend.adapter.persistence.repository;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.TemplateEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for TemplateEntity.
 * All finder methods exclude soft-deleted records (deleted_at IS NULL).
 */
@Repository
public interface TemplateR2dbcRepository extends ReactiveCrudRepository<TemplateEntity, Long> {

    /**
     * Find all non-deleted templates with pagination.
     */
    @Query("SELECT * FROM templates WHERE deleted_at IS NULL")
    Flux<TemplateEntity> findAllActive(Pageable pageable);

    /**
     * Find non-deleted templates by status.
     */
    @Query("SELECT * FROM templates WHERE status = CAST(:status AS template_status) AND deleted_at IS NULL")
    Flux<TemplateEntity> findActiveByStatus(String status, Pageable pageable);

    /**
     * Find non-deleted templates by category.
     */
    @Query("SELECT * FROM templates WHERE category = CAST(:category AS template_category) AND deleted_at IS NULL")
    Flux<TemplateEntity> findActiveByCategory(String category, Pageable pageable);

    /**
     * Find non-deleted templates by status and category.
     */
    @Query("SELECT * FROM templates WHERE status = CAST(:status AS template_status) AND category = CAST(:category AS template_category) AND deleted_at IS NULL")
    Flux<TemplateEntity> findActiveByStatusAndCategory(String status, String category, Pageable pageable);

    /**
     * Search non-deleted templates by name or content (case insensitive).
     */
    @Query("SELECT * FROM templates WHERE (LOWER(name) LIKE LOWER(:name) OR LOWER(content) LIKE LOWER(:content)) AND deleted_at IS NULL")
    Flux<TemplateEntity> searchActive(String name, String content, Pageable pageable);

    /**
     * Find non-deleted template by ID.
     */
    @Query("SELECT * FROM templates WHERE id = :id AND deleted_at IS NULL")
    Mono<TemplateEntity> findActiveById(Long id);

    /**
     * Check if a non-deleted template with the given name exists.
     */
    @Query("SELECT EXISTS (SELECT 1 FROM templates WHERE name = :name AND deleted_at IS NULL)")
    Mono<Boolean> existsActiveByName(String name);

    /**
     * Check if a non-deleted template with the given name exists excluding the given id.
     */
    @Query("SELECT EXISTS (SELECT 1 FROM templates WHERE name = :name AND id != :id AND deleted_at IS NULL)")
    Mono<Boolean> existsActiveByNameAndIdNot(String name, Long id);

    /**
     * Count non-deleted templates by status.
     */
    @Query("SELECT COUNT(*) FROM templates WHERE status = CAST(:status AS template_status) AND deleted_at IS NULL")
    Mono<Long> countActiveByStatus(String status);

    /**
     * Soft delete a template by setting deleted_at = NOW().
     */
    @Query("UPDATE templates SET deleted_at = NOW() WHERE id = :id")
    Mono<Void> softDeleteById(Long id);

    /**
     * Save template with explicit enum casting for PostgreSQL.
     */
    @Query("INSERT INTO templates (name, category, language, status, content, created_at, updated_at) " +
           "VALUES (:#{#entity.name}, CAST(:#{#entity.category} AS template_category), :#{#entity.language}, " +
           "CAST(:#{#entity.status} AS template_status), :#{#entity.content}, :#{#entity.createdAt}, :#{#entity.updatedAt}) " +
           "RETURNING *")
    Mono<TemplateEntity> saveWithEnumCast(TemplateEntity entity);
}
