package com.lewisrp.basemessages.backend.application.service;

import com.lewisrp.basemessages.backend.application.dto.CreateTemplateCommand;
import com.lewisrp.basemessages.backend.application.dto.TemplateDto;
import com.lewisrp.basemessages.backend.application.port.outbound.TemplateRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Template;
import com.lewisrp.basemessages.backend.domain.model.TemplateVariable;
import com.lewisrp.basemessages.backend.adapter.external.MetaApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Application service for template management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateApplicationService {

    private final TemplateRepositoryPort templateRepository;
    private final MetaApiClient metaApiClient;

    /**
     * Create a new template and submit to Meta for approval.
     */
    public Mono<TemplateDto> createTemplate(CreateTemplateCommand command) {
        log.info("Creating template: {}", command.getName());

        // Check if template with same name already exists
        return templateRepository.existsByName(command.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                "Template with name '" + command.getName() + "' already exists"));
                    }

                    // Build template domain model
                    Template template = Template.builder()
                            .name(command.getName())
                            .category(Template.TemplateCategory.valueOf(command.getCategory().toUpperCase()))
                            .language(Template.TemplateLanguage.EN_US) // Default for MVP
                            .status(Template.TemplateStatus.DRAFT)
                            .content(command.getContent())
                            .build();

                    // Build variables
                    if (command.getVariables() != null && !command.getVariables().isEmpty()) {
                        List<TemplateVariable> variables = IntStream.range(0, command.getVariables().size())
                                .mapToObj(i -> TemplateVariable.builder()
                                        .position(i + 1)
                                        .example(command.getVariables().get(i).getExample())
                                        .build())
                                .collect(Collectors.toList());
                        template.setVariables(variables);
                    }

                    // Save template first
                    return templateRepository.save(template)
                            .flatMap(savedTemplate -> {
                                // For MVP, we'll simulate Meta submission
                                // In production, this would call metaApiClient.submitTemplate()
                                log.info("Template saved with ID: {}", savedTemplate.getId());
                                
                                // Transition to PENDING status
                                savedTemplate.submitForApproval();
                                return templateRepository.save(savedTemplate);
                            })
                            .map(this::toDto);
                });
    }

    /**
     * Get all templates with optional filtering.
     */
    public Flux<TemplateDto> listTemplates(String status, String category, String search, Pageable pageable) {
        Flux<Template> templates;

        if (search != null && !search.isEmpty()) {
            templates = templateRepository.search(search, pageable);
        } else if (status != null && category != null) {
            templates = templateRepository.findByStatusAndCategory(status.toUpperCase(), category.toUpperCase(), pageable);
        } else if (status != null) {
            templates = templateRepository.findByStatus(status.toUpperCase(), pageable);
        } else if (category != null) {
            templates = templateRepository.findByCategory(category.toUpperCase(), pageable);
        } else {
            templates = templateRepository.findAll(pageable);
        }

        return templates.map(this::toDto);
    }

    /**
     * Get template by ID.
     */
    public Mono<TemplateDto> getTemplate(Long id) {
        return templateRepository.findById(id)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found with id: " + id)));
    }

    /**
     * Convert domain model to DTO.
     */
    private TemplateDto toDto(Template template) {
        List<TemplateDto.TemplateVariableDto> variables = null;
        if (template.getVariables() != null) {
            variables = template.getVariables().stream()
                    .map(v -> new TemplateDto.TemplateVariableDto(v.getPosition(), v.getExample()))
                    .collect(Collectors.toList());
        }
        
        return new TemplateDto(
                template.getId(),
                template.getName(),
                template.getCategory() != null ? template.getCategory().name() : null,
                template.getLanguage() != null ? template.getLanguage().name() : null,
                template.getStatus() != null ? template.getStatus().name() : null,
                template.getContent(),
                variables,
                template.getRejectionReason(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    /**
     * Exception for not found resources.
     */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
