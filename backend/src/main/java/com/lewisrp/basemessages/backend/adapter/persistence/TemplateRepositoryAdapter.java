package com.lewisrp.basemessages.backend.adapter.persistence;

import com.lewisrp.basemessages.backend.application.port.outbound.TemplateRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Template;
import com.lewisrp.basemessages.backend.adapter.persistence.entity.TemplateEntity;
import com.lewisrp.basemessages.backend.adapter.persistence.entity.TemplateVariableEntity;
import com.lewisrp.basemessages.backend.adapter.persistence.mapper.TemplatePersistenceMapper;
import com.lewisrp.basemessages.backend.adapter.persistence.mapper.TemplateVariablePersistenceMapper;
import com.lewisrp.basemessages.backend.adapter.persistence.repository.TemplateR2dbcRepository;
import com.lewisrp.basemessages.backend.adapter.persistence.repository.TemplateVariableR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adapter implementing the TemplateRepositoryPort using R2DBC.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateRepositoryAdapter implements TemplateRepositoryPort {

    private final TemplateR2dbcRepository templateRepository;
    private final TemplateVariableR2dbcRepository variableRepository;
    private final TemplatePersistenceMapper templateMapper;
    private final TemplateVariablePersistenceMapper variableMapper;
    private final DatabaseClient databaseClient;

    @Override
    public Flux<Template> findAll(Pageable pageable) {
        return templateRepository.findAllBy(pageable)
                .flatMap(this::enrichWithVariables);
    }

    @Override
    public Flux<Template> findByStatus(String status, Pageable pageable) {
        return templateRepository.findByStatus(status, pageable)
                .flatMap(this::enrichWithVariables);
    }

    @Override
    public Flux<Template> findByCategory(String category, Pageable pageable) {
        return templateRepository.findByCategory(category, pageable)
                .flatMap(this::enrichWithVariables);
    }

    @Override
    public Flux<Template> findByStatusAndCategory(String status, String category, Pageable pageable) {
        return templateRepository.findByStatusAndCategory(status, category, pageable)
                .flatMap(this::enrichWithVariables);
    }

    @Override
    public Flux<Template> search(String query, Pageable pageable) {
        return templateRepository.findByNameContainingIgnoreCaseOrContentContainingIgnoreCase(query, query, pageable)
                .flatMap(this::enrichWithVariables);
    }

    @Override
    public Mono<Template> findById(Long id) {
        return templateRepository.findById(id)
                .flatMap(this::enrichWithVariables);
    }

    @Override
    public Mono<Template> save(Template template) {
        // For new templates, use native SQL with explicit enum casting
        if (template.getId() == null) {
            return insertNewTemplate(template);
        }

        // For existing templates, use native SQL with explicit enum casting
        return updateExistingTemplate(template)
                .flatMap(savedEntity -> {
                    // Save variables if present
                    if (template.getVariables() != null && !template.getVariables().isEmpty()) {
                        return saveVariables(savedEntity.getId(), template.getVariables())
                                .then(Mono.just(savedEntity));
                    }
                    return Mono.just(savedEntity);
                })
                .flatMap(this::enrichWithVariables);
    }

    private Mono<TemplateEntity> updateExistingTemplate(Template template) {
        String category = template.getCategory() != null
            ? toDatabaseCategory(template.getCategory())
            : toDatabaseCategory(Template.TemplateCategory.MARKETING);
        String status = template.getStatus() != null
            ? template.getStatus().name()
            : Template.TemplateStatus.DRAFT.name();
        String language = template.getLanguage() != null
            ? template.getLanguage().name()
            : "EN_US";
        LocalDateTime now = LocalDateTime.now();

        return databaseClient.sql("UPDATE templates SET name = :name, category = CAST(:category AS template_category), " +
                "language = :language, status = CAST(:status AS template_status), content = :content, " +
                "rejection_reason = :rejectionReason, updated_at = :updatedAt WHERE id = :id " +
                "RETURNING id, user_id, name, category::text, language, status::text, content, rejection_reason, created_at, updated_at")
                .bind("id", template.getId())
                .bind("name", template.getName())
                .bind("category", category)
                .bind("language", language)
                .bind("status", status)
                .bind("content", template.getContent())
                .bind("rejectionReason", template.getRejectionReason() != null ? template.getRejectionReason() : "")
                .bind("updatedAt", now)
                .map((row, metadata) -> {
                    TemplateEntity entity = new TemplateEntity();
                    entity.setId(row.get("id", Long.class));
                    entity.setName(row.get("name", String.class));
                    entity.setCategory(row.get("category", String.class));
                    entity.setLanguage(row.get("language", String.class));
                    entity.setStatus(row.get("status", String.class));
                    entity.setContent(row.get("content", String.class));
                    entity.setRejectionReason(row.get("rejection_reason", String.class));
                    entity.setCreatedAt(row.get("created_at", LocalDateTime.class));
                    entity.setUpdatedAt(row.get("updated_at", LocalDateTime.class));
                    return entity;
                })
                .one();
    }

    private Mono<Template> insertNewTemplate(Template template) {
        // template_category enum values are TitleCase in this database
        String category = template.getCategory() != null
            ? toDatabaseCategory(template.getCategory())
            : toDatabaseCategory(Template.TemplateCategory.MARKETING);
        String status = template.getStatus() != null 
            ? template.getStatus().name() 
            : Template.TemplateStatus.DRAFT.name();
        String language = template.getLanguage() != null 
            ? template.getLanguage().name() 
            : "EN_US";
        LocalDateTime now = LocalDateTime.now();
        
        return databaseClient.sql("INSERT INTO templates (user_id, name, category, language, status, content, rejection_reason, created_at, updated_at) " +
                "VALUES (1, :name, CAST(:category AS template_category), :language, CAST(:status AS template_status), :content, :rejectionReason, :createdAt, :updatedAt) " +
                "RETURNING id, user_id, name, category::text, language, status::text, content, rejection_reason, created_at, updated_at")
                .bind("name", template.getName())
                .bind("category", category)
                .bind("language", language)
                .bind("status", status)
                .bind("content", template.getContent())
                .bind("rejectionReason", template.getRejectionReason() != null ? template.getRejectionReason() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .map((row, metadata) -> {
                    TemplateEntity entity = new TemplateEntity();
                    entity.setId(row.get("id", Long.class));
                    entity.setName(row.get("name", String.class));
                    entity.setCategory(row.get("category", String.class));
                    entity.setLanguage(row.get("language", String.class));
                    entity.setStatus(row.get("status", String.class));
                    entity.setContent(row.get("content", String.class));
                    entity.setRejectionReason(row.get("rejection_reason", String.class));
                    entity.setCreatedAt(row.get("created_at", LocalDateTime.class));
                    entity.setUpdatedAt(row.get("updated_at", LocalDateTime.class));
                    return entity;
                })
                .one()
                .flatMap(savedEntity -> {
                    // Save variables if present
                    if (template.getVariables() != null && !template.getVariables().isEmpty()) {
                        return saveVariables(savedEntity.getId(), template.getVariables())
                                .then(Mono.just(savedEntity));
                    }
                    return Mono.just(savedEntity);
                })
                .flatMap(this::enrichWithVariables);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return templateRepository.existsByName(name);
    }

    @Override
    public Mono<Long> countByStatus(String status) {
        return templateRepository.countByStatus(status);
    }

    private Mono<Template> enrichWithVariables(TemplateEntity entity) {
        Template template = templateMapper.toDomain(entity);
        
        return variableRepository.findByTemplateId(entity.getId())
                .map(variableMapper::toDomain)
                .collectList()
                .map(variables -> {
                    template.setVariables(variables);
                    return template;
                });
    }

    private Mono<Void> saveVariables(Long templateId, List<com.lewisrp.basemessages.backend.domain.model.TemplateVariable> variables) {
        // First delete existing variables
        return variableRepository.deleteByTemplateId(templateId)
                .thenMany(Flux.fromIterable(variables))
                .map(var -> {
                    TemplateVariableEntity entity = variableMapper.toEntity(var);
                    entity.setTemplateId(templateId);
                    return entity;
                })
                .flatMap(variableRepository::save)
                .then();
    }

    private String toDatabaseCategory(Template.TemplateCategory category) {
        String value = category.name().toLowerCase();
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

}
