package com.lewisrp.basemessages.backend.adapter.web;

import com.lewisrp.basemessages.backend.application.dto.CreateTemplateCommand;
import com.lewisrp.basemessages.backend.application.dto.TemplateDto;
import com.lewisrp.basemessages.backend.application.dto.UpdateTemplateCommand;
import com.lewisrp.basemessages.backend.application.service.TemplateApplicationService;
import org.openapitools.api.TemplatesApiDelegate;
import org.openapitools.model.CreateTemplateRequest;
import org.openapitools.model.Template;
import org.openapitools.model.TemplateDetail;
import org.openapitools.model.TemplateListResponse;
import org.openapitools.model.UpdateTemplateRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the OpenAPI generated TemplatesApiDelegate interface.
 */
@Component
public class TemplatesApiDelegateImpl implements TemplatesApiDelegate {

    private final TemplateApplicationService templateService;

    public TemplatesApiDelegateImpl(TemplateApplicationService templateService) {
        this.templateService = templateService;
    }

    @Override
    public Mono<ResponseEntity<TemplateListResponse>> templatesGet(
            Integer page, Integer limit, String status, String category, String search, ServerWebExchange exchange) {
        
        int pageNum = page != null ? page - 1 : 0; // Convert to 0-based
        int pageSize = limit != null ? limit : 20;
        
        return templateService.listTemplates(status, category, search, PageRequest.of(pageNum, pageSize))
                .collectList()
                .map(templates -> {
                    TemplateListResponse response = new TemplateListResponse();
                    response.setData(templates.stream()
                            .map(this::toApiModel)
                            .collect(Collectors.toList()));
                    
                    // Set pagination info
                    org.openapitools.model.Pagination pagination = new org.openapitools.model.Pagination();
                    pagination.setPage(page != null ? page : 1);
                    pagination.setLimit(pageSize);
                    pagination.setTotal(templates.size());
                    pagination.setTotalPages(1);
                    pagination.setHasNext(false);
                    pagination.setHasPrev(pageNum > 0);
                    response.setPagination(pagination);
                    
                    return ResponseEntity.ok(response);
                });
    }

    @Override
    public Mono<ResponseEntity<Template>> templatesPost(Mono<CreateTemplateRequest> createTemplateRequest, ServerWebExchange exchange) {
        return createTemplateRequest
                .map(this::toCommand)
                .flatMap(templateService::createTemplate)
                .map(this::toApiModel)
                .map(template -> ResponseEntity.status(HttpStatus.CREATED).body(template));
    }

    @Override
    public Mono<ResponseEntity<TemplateDetail>> templatesIdGet(Integer id, ServerWebExchange exchange) {
        return templateService.getTemplate(Long.valueOf(id))
                .map(dto -> {
                    TemplateDetail detail = new TemplateDetail();
                    
                    detail.setId(dto.getId().intValue());
                    detail.setName(dto.getName());
                    
                    // Convert category string to TemplateDetail.CategoryEnum
                    if (dto.getCategory() != null) {
                        detail.setCategory(TemplateDetail.CategoryEnum.valueOf(dto.getCategory()));
                    }
                    
                    detail.setLanguage(dto.getLanguage());
                    
                    // Convert status string to TemplateDetail.StatusEnum
                    if (dto.getStatus() != null) {
                        detail.setStatus(TemplateDetail.StatusEnum.valueOf(dto.getStatus()));
                    }
                    
                    detail.setContent(dto.getContent());
                    
                    // Convert variables
                    if (dto.getVariables() != null) {
                        detail.setVariables(dto.getVariables().stream()
                                .map(v -> {
                                    org.openapitools.model.TemplateVariablesInner var = new org.openapitools.model.TemplateVariablesInner();
                                    var.setPosition(v.getPosition());
                                    var.setExample(v.getExample());
                                    return var;
                                })
                                .collect(Collectors.toList()));
                    }
                    
                    // Convert dates
                    if (dto.getCreatedAt() != null) {
                        detail.setCreatedAt(dto.getCreatedAt().atOffset(ZoneOffset.UTC));
                    }
                    if (dto.getUpdatedAt() != null) {
                        detail.setUpdatedAt(dto.getUpdatedAt().atOffset(ZoneOffset.UTC));
                    }
                    
                    detail.setRejectionReason(dto.getRejectionReason());
                    detail.setMetaError(dto.getMetaError());
                    return detail;
                })
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<Template>> templatesIdResubmitPost(Integer id, ServerWebExchange exchange) {
        return templateService.resubmitTemplate(Long.valueOf(id))
                .map(this::toApiModel)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Template>> templatesIdRefreshStatusPost(Integer id, ServerWebExchange exchange) {
        return templateService.refreshTemplateStatus(Long.valueOf(id))
                .map(this::toApiModel)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> templatesIdDelete(Integer id, ServerWebExchange exchange) {
        return templateService.deleteTemplate(Long.valueOf(id))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @Override
    public Mono<ResponseEntity<Template>> templatesIdPut(Integer id, Mono<UpdateTemplateRequest> updateTemplateRequest, ServerWebExchange exchange) {
        return updateTemplateRequest
                .map(this::toUpdateCommand)
                .flatMap(command -> templateService.updateTemplate(Long.valueOf(id), command))
                .map(this::toApiModel)
                .map(ResponseEntity::ok);
    }

    private CreateTemplateCommand toCommand(CreateTemplateRequest request) {
        List<CreateTemplateCommand.TemplateVariableDto> variables = null;
        if (request.getVariables() != null) {
            variables = request.getVariables().stream()
                    .map(v -> new CreateTemplateCommand.TemplateVariableDto(v.getExample()))
                    .collect(Collectors.toList());
        }
        
        return new CreateTemplateCommand(
                request.getName(),
                request.getCategory() != null ? request.getCategory().name() : null,
                request.getContent(),
                request.getLanguage(),
                variables
        );
    }

    private UpdateTemplateCommand toUpdateCommand(UpdateTemplateRequest request) {
        List<UpdateTemplateCommand.TemplateVariableDto> variables = null;
        if (request.getVariables() != null) {
            variables = request.getVariables().stream()
                    .map(v -> {
                        if (v instanceof java.util.Map<?, ?> map) {
                            Object example = map.get("example");
                            return new UpdateTemplateCommand.TemplateVariableDto(example != null ? example.toString() : null);
                        }
                        return new UpdateTemplateCommand.TemplateVariableDto(null);
                    })
                    .collect(Collectors.toList());
        }

        return new UpdateTemplateCommand(
                request.getName(),
                request.getContent(),
                variables
        );
    }

    private Template toApiModel(TemplateDto dto) {
        Template template = new Template();
        template.setId(dto.getId().intValue());
        template.setName(dto.getName());
        
        // Convert category string to enum
        if (dto.getCategory() != null) {
            template.setCategory(Template.CategoryEnum.valueOf(dto.getCategory()));
        }
        
        template.setLanguage(dto.getLanguage());
        
        // Convert status string to enum
        if (dto.getStatus() != null) {
            template.setStatus(Template.StatusEnum.valueOf(dto.getStatus()));
        }
        
        template.setContent(dto.getContent());
        
        if (dto.getVariables() != null) {
            template.setVariables(dto.getVariables().stream()
                    .map(v -> {
                        org.openapitools.model.TemplateVariablesInner var = new org.openapitools.model.TemplateVariablesInner();
                        var.setPosition(v.getPosition());
                        var.setExample(v.getExample());
                        return var;
                    })
                    .collect(Collectors.toList()));
        }
        
        // Convert LocalDateTime to OffsetDateTime
        if (dto.getCreatedAt() != null) {
            template.setCreatedAt(dto.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        if (dto.getUpdatedAt() != null) {
            template.setUpdatedAt(dto.getUpdatedAt().atOffset(ZoneOffset.UTC));
        }

        template.setMetaError(dto.getMetaError());

        return template;
    }
}
