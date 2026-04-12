package com.lewisrp.basemessages.backend.adapter.persistence.repository;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.TemplateVariableEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Spring Data R2DBC repository for TemplateVariableEntity.
 */
@Repository
public interface TemplateVariableR2dbcRepository extends ReactiveCrudRepository<TemplateVariableEntity, Long> {

    /**
     * Find all variables for a specific template.
     */
    Flux<TemplateVariableEntity> findByTemplateId(Long templateId);

    /**
     * Delete all variables for a specific template.
     */
    Flux<Void> deleteByTemplateId(Long templateId);
}
