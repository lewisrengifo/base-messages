package com.lewisrp.basemessages.backend.adapter.web;

import com.lewisrp.basemessages.backend.application.dto.CampaignDto;
import com.lewisrp.basemessages.backend.application.dto.CampaignPageDto;
import com.lewisrp.basemessages.backend.application.dto.CreateCampaignCommand;
import com.lewisrp.basemessages.backend.application.service.CampaignApplicationService;
import org.openapitools.api.CampaignsApiDelegate;
import org.openapitools.model.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

/**
 * Implementation of the OpenAPI generated CampaignsApiDelegate interface.
 */
@Component
public class CampaignsApiDelegateImpl implements CampaignsApiDelegate {

    private final CampaignApplicationService campaignService;

    public CampaignsApiDelegateImpl(CampaignApplicationService campaignService) {
        this.campaignService = campaignService;
    }

    @Override
    public Mono<ResponseEntity<CampaignListResponse>> campaignsGet(
            Integer page,
            Integer limit,
            String status,
            String search,
            ServerWebExchange exchange) {

        int pageNum = page != null ? page - 1 : 0;
        int pageSize = limit != null ? limit : 20;

        return campaignService.listCampaigns(status, search, PageRequest.of(pageNum, pageSize))
                .map(pageDto -> {
                    CampaignListResponse response = new CampaignListResponse();
                    response.setData(pageDto.getData().stream()
                            .map(this::toApiModel)
                            .collect(Collectors.toList()));

                    long total = pageDto.getTotal();
                    long totalPages = pageSize > 0 ? (long) Math.ceil((double) total / pageSize) : 1;
                    boolean hasPrev = pageNum > 0;
                    boolean hasNext = (long) (pageNum + 1) * pageSize < total;

                    Pagination pagination = new Pagination();
                    pagination.setPage(page != null ? page : 1);
                    pagination.setLimit(pageSize);
                    pagination.setTotal((int) total);
                    pagination.setTotalPages((int) Math.max(1, totalPages));
                    pagination.setHasNext(hasNext);
                    pagination.setHasPrev(hasPrev);
                    response.setPagination(pagination);

                    return ResponseEntity.ok(response);
                });
    }

    @Override
    public Mono<ResponseEntity<Campaign>> campaignsPost(
            Mono<CreateCampaignRequest> createCampaignRequest,
            ServerWebExchange exchange) {
        return createCampaignRequest
                .map(this::toCommand)
                .flatMap(campaignService::createCampaign)
                .map(this::toApiModel)
                .map(campaign -> ResponseEntity.status(HttpStatus.CREATED).body(campaign))
                .onErrorResume(CampaignApplicationService.NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(IllegalStateException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @Override
    public Mono<ResponseEntity<CampaignDetail>> campaignsIdGet(String id, ServerWebExchange exchange) {
        return campaignService.getCampaign(id)
                .map(this::toApiDetail)
                .map(ResponseEntity::ok)
                .onErrorResume(CampaignApplicationService.NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> campaignsIdDelete(String id, ServerWebExchange exchange) {
        return campaignService.deleteCampaign(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(CampaignApplicationService.NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<Campaign>> campaignsIdCancelPost(String id, ServerWebExchange exchange) {
        return campaignService.cancelCampaign(id)
                .map(this::toApiModel)
                .map(ResponseEntity::ok)
                .onErrorResume(CampaignApplicationService.NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(IllegalStateException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }

    @Override
    public Mono<ResponseEntity<Campaign>> campaignsIdDuplicatePost(String id, ServerWebExchange exchange) {
        return campaignService.duplicateCampaign(id)
                .map(this::toApiModel)
                .map(campaign -> ResponseEntity.status(HttpStatus.CREATED).body(campaign))
                .onErrorResume(CampaignApplicationService.NotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<CampaignAnalytics>> campaignsIdAnalyticsGet(String id, ServerWebExchange exchange) {
        // MVP: analytics not fully implemented
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build());
    }

    @Override
    public Mono<ResponseEntity<Object>> campaignsIdAnalyticsExportGet(String id, String format, ServerWebExchange exchange) {
        // MVP: analytics export not implemented
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build());
    }

    private CreateCampaignCommand toCommand(CreateCampaignRequest request) {
        LocalDateTime scheduledAt = null;
        if (request.getSchedule() != null && request.getSchedule().getType() == CreateCampaignRequestSchedule.TypeEnum.SCHEDULED) {
            LocalDate date = request.getSchedule().getDate();
            String timeStr = request.getSchedule().getTime();
            if (date != null && timeStr != null) {
                LocalTime time = LocalTime.parse(timeStr);
                scheduledAt = LocalDateTime.of(date, time);
            }
        }

        return new CreateCampaignCommand(
                request.getName(),
                request.getTemplateId() != null ? request.getTemplateId().longValue() : null,
                request.getAudience() != null ? request.getAudience().getType().getValue() : null,
                request.getAudience() != null ? request.getAudience().getGroupId() : null,
                request.getSchedule() != null ? request.getSchedule().getType().getValue() : null,
                scheduledAt
        );
    }

    private Campaign toApiModel(CampaignDto dto) {
        Campaign campaign = new Campaign();
        campaign.setId(dto.getCampaignCode());
        campaign.setName(dto.getName());
        campaign.setTemplateName(dto.getTemplateName());
        if (dto.getScheduledAt() != null) {
            campaign.setScheduledDate(dto.getScheduledAt().atOffset(ZoneOffset.UTC));
        }
        if (dto.getStatus() != null) {
            campaign.setStatus(Campaign.StatusEnum.fromValue(dto.getStatus()));
        }
        if (dto.getCreatedAt() != null) {
            campaign.setCreatedAt(dto.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        if (dto.getUpdatedAt() != null) {
            campaign.setUpdatedAt(dto.getUpdatedAt().atOffset(ZoneOffset.UTC));
        }
        return campaign;
    }

    private CampaignDetail toApiDetail(CampaignDto dto) {
        CampaignDetail detail = new CampaignDetail();
        detail.setId(dto.getCampaignCode());
        detail.setName(dto.getName());
        detail.setTemplateName(dto.getTemplateName());
        if (dto.getScheduledAt() != null) {
            detail.setScheduledDate(dto.getScheduledAt().atOffset(ZoneOffset.UTC));
        }
        if (dto.getStatus() != null) {
            detail.setStatus(CampaignDetail.StatusEnum.fromValue(dto.getStatus()));
        }
        if (dto.getCreatedAt() != null) {
            detail.setCreatedAt(dto.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        if (dto.getUpdatedAt() != null) {
            detail.setUpdatedAt(dto.getUpdatedAt().atOffset(ZoneOffset.UTC));
        }

        // Minimal template info
        Template template = new Template();
        template.setId(dto.getId() != null ? dto.getId().intValue() : null);
        template.setName(dto.getTemplateName());
        detail.setTemplate(template);

        // Audience
        CampaignDetailAllOfAudience audience = new CampaignDetailAllOfAudience();
        audience.setGroup(dto.getAudienceType());
        audience.setRecipientCount(dto.getRecipientCount());
        detail.setAudience(audience);

        // Schedule
        CampaignDetailAllOfSchedule schedule = new CampaignDetailAllOfSchedule();
        if (dto.getScheduledAt() != null) {
            schedule.setDate(dto.getScheduledAt().toLocalDate());
            schedule.setTime(dto.getScheduledAt().toLocalTime().toString());
        }
        detail.setSchedule(schedule);

        return detail;
    }
}
