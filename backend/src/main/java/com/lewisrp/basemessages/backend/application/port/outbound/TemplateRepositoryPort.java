package com.lewisrp.basemessages.backend.application.port.outbound;

import com.lewisrp.basemessages.backend.domain.model.Template;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port for template persistence operations.
 */
public interface TemplateRepositoryPort {

    /**
     * Find all templates with pagination.
     */
    Flux<Template> findAll(Pageable pageable);

    /**
     * Find templates by status.
     */
    Flux<Template> findByStatus(String status, Pageable pageable);

    /**
     * Find templates by category.
     */
    Flux<Template> findByCategory(String category, Pageable pageable);

    /**
     * Find templates by status and category.
     */
    Flux<Template> findByStatusAndCategory(String status, String category, Pageable pageable);

    /**
     * Search templates by name or content.
     */
    Flux<Template> search(String query, Pageable pageable);

    /**
     * Find template by ID.
     */
    Mono<Template> findById(Long id);

    /**
     * Save a template.
     */
    Mono<Template> save(Template template);

    /**
     * Check if template with name exists.
     */
    Mono<Boolean> existsByName(String name);

    /**
     * Count templates by status.
     */
    Mono<Long> countByStatus(String status);
}
