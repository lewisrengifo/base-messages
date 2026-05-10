package com.lewisrp.basemessages.backend.application.service;

import com.lewisrp.basemessages.backend.adapter.external.MetaApiClient;
import com.lewisrp.basemessages.backend.adapter.external.MetaApiClient.SubmissionResult;
import com.lewisrp.basemessages.backend.adapter.external.MetaApiClient.TemplateStatusResult;
import com.lewisrp.basemessages.backend.adapter.security.EncryptionService;
import com.lewisrp.basemessages.backend.adapter.storage.StorageService;
import com.lewisrp.basemessages.backend.application.dto.CreateTemplateCommand;
import com.lewisrp.basemessages.backend.application.dto.TemplateDto;
import com.lewisrp.basemessages.backend.application.dto.UpdateTemplateCommand;
import com.lewisrp.basemessages.backend.application.exception.MetaSubmissionException;
import com.lewisrp.basemessages.backend.application.port.outbound.ConnectionRepositoryPort;
import com.lewisrp.basemessages.backend.application.port.outbound.TemplateRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Connection;
import com.lewisrp.basemessages.backend.domain.model.Template;
import com.lewisrp.basemessages.backend.domain.model.TemplateVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateApplicationService {

    private final TemplateRepositoryPort templateRepository;
    private final ConnectionRepositoryPort connectionRepository;
    private final MetaApiClient metaApiClient;
    private final EncryptionService encryptionService;
    private final StorageService storageService;

    public Mono<TemplateDto> createTemplate(CreateTemplateCommand command) {
        log.info("Creating template: {}", command.getName());

        String nameValidationError = validateTemplateName(command.getName());
        if (nameValidationError != null) {
            return Mono.error(new IllegalArgumentException(nameValidationError));
        }

        return templateRepository.existsByName(command.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                "Template with name '" + command.getName() + "' already exists"));
                    }

                    Template.TemplateLanguage language = command.getLanguage() != null
                            ? parseLanguage(command.getLanguage())
                            : Template.TemplateLanguage.EN_US;

                    Template.HeaderType headerType = parseHeaderType(command.getHeaderType());

                    Template template = Template.builder()
                            .name(command.getName())
                            .category(Template.TemplateCategory.valueOf(command.getCategory().toUpperCase()))
                            .language(language)
                            .status(Template.TemplateStatus.PENDING)
                            .content(command.getContent())
                            .headerType(headerType)
                            .build();

                    if (command.getVariables() != null && !command.getVariables().isEmpty()) {
                        List<TemplateVariable> variables = IntStream.range(0, command.getVariables().size())
                                .mapToObj(i -> TemplateVariable.builder()
                                        .position(i + 1)
                                        .example(command.getVariables().get(i).getExample())
                                        .build())
                                .collect(Collectors.toList());
                        template.setVariables(variables);
                    }

                    // Upload document to S3 if provided
                    Mono<Template> uploadMono = Mono.just(template);
                    if (headerType == Template.HeaderType.DOCUMENT && command.getHeaderDocument() != null) {
                        uploadMono = Mono.fromCallable(() -> {
                            String key = storageService.uploadFile(
                                    "templates",
                                    command.getHeaderDocumentName(),
                                    command.getHeaderDocument(),
                                    "application/pdf"
                            );
                            template.setHeaderDocumentKey(key);
                            String presignedUrl = storageService.generatePresignedUrl(key);
                            template.setHeaderDocumentUrl(presignedUrl);
                            return template;
                        });
                    }

                    return uploadMono
                            .flatMap(templateRepository::save)
                            .flatMap(savedTemplate -> submitToMeta(savedTemplate, command.getHeaderDocument()));
                });
    }

    public Mono<TemplateDto> resubmitTemplate(Long id) {
        log.info("Resubmitting template: {}", id);

        return templateRepository.findById(id)
                .flatMap(template -> {
                    if (!template.canBeResubmitted()) {
                        return Mono.error(new IllegalStateException(
                                "Only rejected templates or templates with a previous submission error can be resubmitted"));
                    }

                    // Clear previous error before retry
                    template.setMetaError(null);

                    // Transition rejected templates back to pending
                    if (template.getStatus() == Template.TemplateStatus.REJECTED) {
                        template.submitForApproval();
                    }

                    return submitToMeta(template);
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found with id: " + id)));
    }

    public Mono<TemplateDto> refreshTemplateStatus(Long id) {
        log.info("Refreshing template status from Meta: {}", id);

        return templateRepository.findById(id)
                .flatMap(template -> {
                    if (template.getMetaTemplateId() == null || template.getMetaTemplateId().isBlank()) {
                        return Mono.error(new IllegalStateException(
                                "This template has not been submitted to Meta yet"));
                    }

                    return connectionRepository.findCurrent()
                            .flatMap(connection -> {
                                String accessToken = encryptionService.decrypt(connection.getAccessToken());
                                return metaApiClient.getTemplateStatus(
                                        template.getMetaTemplateId(),
                                        connection.getWabaId(),
                                        accessToken
                                );
                            })
                            .flatMap(statusResult -> {
                                String metaStatus = statusResult.status();
                                log.info("Meta status for template {}: {}", id, metaStatus);

                                return switch (metaStatus.toUpperCase()) {
                                    case "APPROVED" -> {
                                        template.approve();
                                        template.setMetaError(null);
                                        yield templateRepository.save(template).map(this::toDto);
                                    }
                                    case "REJECTED" -> {
                                        String reason = statusResult.rejectionReason();
                                        if (reason == null || reason.isBlank()) {
                                            reason = template.getRejectionReason() != null ? template.getRejectionReason() : "Rejected by Meta";
                                        }
                                        template.reject(reason);
                                        template.setMetaError(null);
                                        yield templateRepository.save(template).map(this::toDto);
                                    }
                                    case "PENDING" -> Mono.just(template).map(this::toDto);
                                    case "ERROR" -> Mono.error(new IllegalStateException(
                                            "Failed to fetch status from Meta. Please try again."));
                                    default -> {
                                        log.warn("Unknown Meta status '{}', keeping current status", metaStatus);
                                        yield Mono.just(template).map(this::toDto);
                                    }
                                };
                            })
                            .switchIfEmpty(Mono.error(new IllegalStateException(
                                    "No connection found. Cannot refresh status from Meta.")));
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found with id: " + id)));
    }

    public Mono<TemplateDto> updateTemplate(Long id, UpdateTemplateCommand command) {
        log.info("Updating template: {}", id);

        if (command.getName() != null) {
            String nameValidationError = validateTemplateName(command.getName());
            if (nameValidationError != null) {
                return Mono.error(new IllegalArgumentException(nameValidationError));
            }
        }

        return templateRepository.findById(id)
                .flatMap(template -> {
                    if (!template.canBeUpdated()) {
                        return Mono.error(new IllegalStateException(
                                "Only REJECTED or PENDING templates with a previous error can be updated"));
                    }

                    // Check name uniqueness if changed
                    Mono<Boolean> nameCheck = Mono.just(false);
                    if (command.getName() != null && !command.getName().equals(template.getName())) {
                        nameCheck = templateRepository.existsByNameAndIdNot(command.getName(), id);
                    }

                    return nameCheck.flatMap(exists -> {
                        if (exists) {
                            return Mono.error(new IllegalArgumentException(
                                    "Template with name '" + command.getName() + "' already exists"));
                        }

                        // Apply updates
                        if (command.getName() != null) {
                            template.setName(command.getName());
                        }
                        if (command.getContent() != null) {
                            template.setContent(command.getContent());
                        }
                        if (command.getVariables() != null) {
                            List<TemplateVariable> variables = IntStream.range(0, command.getVariables().size())
                                    .mapToObj(i -> TemplateVariable.builder()
                                            .position(i + 1)
                                            .example(command.getVariables().get(i).getExample())
                                            .build())
                                    .collect(Collectors.toList());
                            template.setVariables(variables);
                        }

                        return templateRepository.save(template)
                                .map(this::toDto);
                    });
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found with id: " + id)));
    }

    private Mono<TemplateDto> submitToMeta(Template template) {
        return submitToMeta(template, null);
    }

    private Mono<TemplateDto> submitToMeta(Template template, byte[] documentBytes) {
        return connectionRepository.findCurrent()
                .flatMap(connection -> {
                    String accessToken = encryptionService.decrypt(connection.getAccessToken());
                    final List<HashMap<String, String>> metaVariables;
                    if (template.getVariables() != null && !template.getVariables().isEmpty()) {
                        metaVariables = template.getVariables().stream()
                                .map(v -> {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("example", v.getExample());
                                    return map;
                                })
                                .collect(Collectors.toList());
                    } else {
                        metaVariables = null;
                    }

                    final Template currentTemplate = template;

                    // Build the submission Mono
                    Mono<SubmissionResult> submitMono;
                    if (currentTemplate.getHeaderType() == Template.HeaderType.DOCUMENT
                            && currentTemplate.getHeaderDocumentKey() != null) {
                        submitMono = Mono.fromCallable(() -> {
                                    byte[] bytes = documentBytes;
                                    if (bytes == null) {
                                        bytes = storageService.downloadFile(currentTemplate.getHeaderDocumentKey());
                                    }
                                    String fileName = currentTemplate.getHeaderDocumentKey();
                                    int lastSlash = fileName.lastIndexOf('/');
                                    if (lastSlash >= 0) {
                                        fileName = fileName.substring(lastSlash + 1);
                                    }
                                    return metaApiClient.uploadMedia(
                                            connection.getWabaId(),
                                            accessToken,
                                            bytes,
                                            fileName,
                                            "application/pdf"
                                    ).block();
                                })
                                .onErrorResume(error -> {
                                    log.error("Failed to upload document to Meta for template {}: {}",
                                            currentTemplate.getName(), error.getMessage());
                                    return Mono.empty();
                                })
                                .flatMap(headerHandle -> {
                                    currentTemplate.setHeaderHandle(headerHandle);
                                    return metaApiClient.submitTemplate(
                                            connection.getWabaId(),
                                            accessToken,
                                            currentTemplate.getName(),
                                            currentTemplate.getCategory().name(),
                                            languageToCode(currentTemplate.getLanguage()),
                                            currentTemplate.getContent(),
                                            metaVariables != null ? new ArrayList<>(metaVariables) : null,
                                            currentTemplate.getHeaderType() != null ? currentTemplate.getHeaderType().name() : null,
                                            headerHandle
                                    );
                                });
                    } else {
                        currentTemplate.setHeaderHandle(null);
                        submitMono = metaApiClient.submitTemplate(
                                connection.getWabaId(),
                                accessToken,
                                currentTemplate.getName(),
                                currentTemplate.getCategory().name(),
                                languageToCode(currentTemplate.getLanguage()),
                                currentTemplate.getContent(),
                                metaVariables != null ? new ArrayList<>(metaVariables) : null,
                                currentTemplate.getHeaderType() != null ? currentTemplate.getHeaderType().name() : null,
                                null
                        );
                    }

                    return submitMono
                            .flatMap(result -> handleSubmissionResult(result, currentTemplate))
                            .onErrorResume(error -> {
                                log.error("Meta submission failed for template {}: {}", currentTemplate.getName(), error.getMessage());
                                if (currentTemplate.getStatus() != Template.TemplateStatus.PENDING) {
                                    currentTemplate.submitForApproval();
                                }
                                currentTemplate.setMetaError(error.getMessage());
                                return templateRepository.save(currentTemplate)
                                        .flatMap(saved -> Mono.error(new MetaSubmissionException(
                                                "Template submission to Meta failed", error.getMessage())));
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No connection found - saving template locally as PENDING without Meta submission");
                    if (template.getStatus() != Template.TemplateStatus.PENDING) {
                        template.submitForApproval();
                    }
                    return templateRepository.save(template).map(this::toDto);
                }));
    }

    private Mono<TemplateDto> handleSubmissionResult(SubmissionResult result, Template template) {
        if (result.success()) {
            log.info("Template submitted to Meta successfully - metaId: {}", result.metaTemplateId());
            if (template.getStatus() != Template.TemplateStatus.PENDING) {
                template.submitForApproval();
            }
            template.setMetaError(null);
            if (result.metaTemplateId() != null) {
                template.setMetaTemplateId(result.metaTemplateId());
            }
            return templateRepository.save(template).map(this::toDto);
        } else {
            log.error("Meta submission failed: {} - saving template as PENDING for retry", result.errorMessage());
            if (template.getStatus() != Template.TemplateStatus.PENDING) {
                template.submitForApproval();
            }
            template.setMetaError(result.errorMessage());
            return templateRepository.save(template)
                    .flatMap(saved -> Mono.error(new MetaSubmissionException(
                            "Template submission to Meta failed", result.errorMessage())));
        }
    }

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

    public Mono<TemplateDto> getTemplate(Long id) {
        return templateRepository.findById(id)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found with id: " + id)));
    }

    public Mono<Void> deleteTemplate(Long id) {
        log.info("Deleting template: {}", id);
        return templateRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found with id: " + id)))
                .flatMap(template -> {
                    // Delete document from S3 if exists
                    if (template.getHeaderDocumentKey() != null) {
                        try {
                            storageService.deleteFile(template.getHeaderDocumentKey());
                        } catch (Exception e) {
                            log.warn("Failed to delete S3 file for template {}: {}", id, e.getMessage());
                        }
                    }
                    return templateRepository.deleteById(id);
                });
    }

    private TemplateDto toDto(Template template) {
        List<TemplateDto.TemplateVariableDto> variables = null;
        if (template.getVariables() != null) {
            variables = template.getVariables().stream()
                    .map(v -> new TemplateDto.TemplateVariableDto(v.getPosition(), v.getExample()))
                    .collect(Collectors.toList());
        }

        // Generate fresh presigned URL if document exists
        String headerDocumentUrl = template.getHeaderDocumentUrl();
        if (template.getHeaderType() == Template.HeaderType.DOCUMENT && template.getHeaderDocumentKey() != null) {
            try {
                headerDocumentUrl = storageService.generatePresignedUrl(template.getHeaderDocumentKey());
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for template {}: {}", template.getId(), e.getMessage());
            }
        }

        return new TemplateDto(
                template.getId(),
                template.getName(),
                template.getCategory() != null ? template.getCategory().name() : null,
                template.getLanguage() != null ? template.getLanguage().name() : null,
                template.getStatus() != null ? template.getStatus().name() : null,
                template.getContent(),
                template.getHeaderType() != null ? template.getHeaderType().name() : null,
                headerDocumentUrl,
                variables,
                template.getRejectionReason(),
                template.getMetaError(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                template.getDeletedAt()
        );
    }

    private Template.TemplateLanguage parseLanguage(String language) {
        try {
            return Template.TemplateLanguage.valueOf(language.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown language code '{}', defaulting to EN_US", language);
            return Template.TemplateLanguage.EN_US;
        }
    }

    private Template.HeaderType parseHeaderType(String headerType) {
        if (headerType == null || headerType.isBlank()) {
            return Template.HeaderType.TEXT;
        }
        try {
            return Template.HeaderType.valueOf(headerType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown header type '{}', defaulting to TEXT", headerType);
            return Template.HeaderType.TEXT;
        }
    }

    private String languageToCode(Template.TemplateLanguage language) {
        String name = language.name();
        int idx = name.indexOf('_');
        if (idx > 0) {
            return name.substring(0, idx).toLowerCase() + name.substring(idx);
        }
        return name.toLowerCase();
    }

    /**
     * Validates that a template name meets Meta's requirements.
     * Meta requires names to contain only lowercase letters, numbers, and underscores.
     *
     * @return null if valid, error message otherwise
     */
    private String validateTemplateName(String name) {
        if (name == null || name.isBlank()) {
            return "Template name is required";
        }
        if (!name.matches("^[a-z0-9_]+$")) {
            return "Template name can only contain lowercase letters, numbers, and underscores";
        }
        return null;
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
