package com.lewisrp.basemessages.backend.application.service;

import com.lewisrp.basemessages.backend.application.dto.CampaignDto;
import com.lewisrp.basemessages.backend.application.dto.CampaignPageDto;
import com.lewisrp.basemessages.backend.application.dto.CreateCampaignCommand;
import com.lewisrp.basemessages.backend.adapter.scheduler.CampaignSchedulerService;
import com.lewisrp.basemessages.backend.application.port.outbound.CampaignRepositoryPort;
import com.lewisrp.basemessages.backend.application.port.outbound.ContactRepositoryPort;
import com.lewisrp.basemessages.backend.application.port.outbound.TemplateRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Campaign;
import com.lewisrp.basemessages.backend.domain.model.CampaignRecipient;
import com.lewisrp.basemessages.backend.domain.model.Contact;
import com.lewisrp.basemessages.backend.domain.model.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service for campaign management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignApplicationService {

    private static final long MVP_USER_ID = 1L;

    private final CampaignRepositoryPort campaignRepository;
    private final TemplateRepositoryPort templateRepository;
    private final ContactRepositoryPort contactRepository;
    private final BroadcastService broadcastService;
    private final CampaignSchedulerService schedulerService;

    /**
     * Create a new campaign with recipient snapshot.
     */
    public Mono<CampaignDto> createCampaign(CreateCampaignCommand command) {
        String normalizedName = command.getName() != null ? command.getName().trim() : "";

        if (normalizedName.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Campaign name is required"));
        }
        if (command.getTemplateId() == null) {
            return Mono.error(new IllegalArgumentException("Template ID is required"));
        }
        if (command.getAudienceType() == null || command.getAudienceType().isBlank()) {
            return Mono.error(new IllegalArgumentException("Audience type is required"));
        }
        if (command.getScheduleType() == null || command.getScheduleType().isBlank()) {
            return Mono.error(new IllegalArgumentException("Schedule type is required"));
        }

        return templateRepository.findById(command.getTemplateId())
                .flatMap(template -> {
                    if (template.getStatus() != Template.TemplateStatus.APPROVED) {
                        return Mono.error(new IllegalArgumentException(
                                "Template must be APPROVED to create a campaign. Current status: " + template.getStatus()));
                    }

                    return resolveContactIds(command.getAudienceType(), command.getAudienceGroupId())
                            .flatMap(contactIds -> {
                                if (contactIds.isEmpty()) {
                                    return Mono.error(new IllegalArgumentException(
                                            "No contacts found for the selected audience"));
                                }

                                Campaign.ScheduleType scheduleType;
                                try {
                                    scheduleType = Campaign.ScheduleType.valueOf(command.getScheduleType().toUpperCase());
                                } catch (IllegalArgumentException e) {
                                    return Mono.error(new IllegalArgumentException("Invalid schedule type: " + command.getScheduleType()));
                                }

                                Campaign.AudienceType audienceType;
                                try {
                                    audienceType = Campaign.AudienceType.valueOf(command.getAudienceType().toUpperCase());
                                } catch (IllegalArgumentException e) {
                                    return Mono.error(new IllegalArgumentException("Invalid audience type: " + command.getAudienceType()));
                                }

                                LocalDateTime scheduledAt = command.getScheduledAt();
                                Campaign campaign = Campaign.builder()
                                        .userId(MVP_USER_ID)
                                        .name(normalizedName)
                                        .templateId(command.getTemplateId())
                                        .status(Campaign.CampaignStatus.DRAFT)
                                        .audienceType(audienceType)
                                        .audienceGroupId(parseGroupId(command.getAudienceGroupId()))
                                        .recipientCount(contactIds.size())
                                        .scheduleType(scheduleType)
                                        .scheduledAt(scheduledAt)
                                        .scheduledDate(scheduledAt != null ? scheduledAt.toLocalDate() : null)
                                        .scheduledTime(scheduledAt != null ? scheduledAt.toLocalTime() : null)
                                        .estimatedCost(java.math.BigDecimal.ZERO)
                                        .build();

                                return campaignRepository.save(campaign)
                                        .flatMap(savedCampaign -> {
                                            List<CampaignRecipient> recipients = contactIds.stream()
                                                    .map(contactId -> CampaignRecipient.builder()
                                                            .campaignId(savedCampaign.getId())
                                                            .contactId(contactId)
                                                            .status(CampaignRecipient.MessageStatus.PENDING)
                                                            .build())
                                                    .toList();

                                            return campaignRepository.saveRecipients(recipients)
                                                    .then(Mono.just(savedCampaign));
                                        })
                                        .flatMap(savedCampaign -> {
                                            if (savedCampaign.getScheduleType() == Campaign.ScheduleType.IMMEDIATE) {
                                                savedCampaign.markSending();
                                                return campaignRepository.save(savedCampaign)
                                                        .doOnSuccess(c -> broadcastService.executeCampaign(c.getId())
                                                                .subscribe(
                                                                        null,
                                                                        error -> log.error("Immediate broadcast failed for campaign {}", c.getId(), error)
                                                                ))
                                                        .thenReturn(savedCampaign);
                                            } else if (savedCampaign.getScheduleType() == Campaign.ScheduleType.SCHEDULED
                                                    && savedCampaign.getScheduledAt() != null) {
                                                return schedulerService.scheduleCampaign(savedCampaign.getId(), savedCampaign.getScheduledAt())
                                                        .thenReturn(savedCampaign)
                                                        .onErrorResume(e -> {
                                                            log.error("Failed to schedule campaign {}", savedCampaign.getId(), e);
                                                            return Mono.just(savedCampaign);
                                                        });
                                            }
                                            return Mono.just(savedCampaign);
                                        })
                                        .flatMap(this::toDto);
                            });
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found with id: " + command.getTemplateId())));
    }

    /**
     * List campaigns with optional status filter and search.
     */
    public Mono<CampaignPageDto> listCampaigns(String status, String search, Pageable pageable) {
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasSearch = search != null && !search.isBlank();
        String normalizedSearch = hasSearch ? search.trim() : null;
        String normalizedStatus = hasStatus ? status.toUpperCase() : null;

        Mono<List<CampaignDto>> dataMono;
        Mono<Long> totalMono;

        if (hasSearch) {
            dataMono = campaignRepository.search(normalizedSearch, pageable)
                    .flatMap(this::toDto)
                    .collectList();
            totalMono = campaignRepository.countSearch(normalizedSearch);
        } else if (hasStatus) {
            dataMono = campaignRepository.findByStatus(normalizedStatus, pageable)
                    .flatMap(this::toDto)
                    .collectList();
            totalMono = campaignRepository.countByStatus(normalizedStatus);
        } else {
            dataMono = campaignRepository.findAll(pageable)
                    .flatMap(this::toDto)
                    .collectList();
            totalMono = campaignRepository.countAll();
        }

        return Mono.zip(dataMono, totalMono)
                .map(tuple -> new CampaignPageDto(tuple.getT1(), tuple.getT2()));
    }

    /**
     * Get campaign by campaign code.
     */
    public Mono<CampaignDto> getCampaign(String campaignCode) {
        return campaignRepository.findByCampaignCode(campaignCode)
                .flatMap(this::toDto)
                .switchIfEmpty(Mono.error(new NotFoundException("Campaign not found with code: " + campaignCode)));
    }

    /**
     * Cancel a campaign that is in DRAFT or SCHEDULED status.
     */
    public Mono<CampaignDto> cancelCampaign(String campaignCode) {
        return campaignRepository.findByCampaignCode(campaignCode)
                .flatMap(campaign -> {
                    if (!campaign.canBeCanceled()) {
                        return Mono.error(new IllegalStateException(
                                "Cannot cancel campaign in status: " + campaign.getStatus()));
                    }
                    campaign.cancel("Canceled by user");
                    return campaignRepository.save(campaign)
                            .flatMap(saved -> schedulerService.unscheduleCampaign(saved.getId())
                                    .thenReturn(saved)
                                    .onErrorResume(e -> {
                                        log.error("Failed to unschedule campaign {}", saved.getId(), e);
                                        return Mono.just(saved);
                                    }));
                })
                .flatMap(this::toDto)
                .switchIfEmpty(Mono.error(new NotFoundException("Campaign not found with code: " + campaignCode)));
    }

    /**
     * Duplicate an existing campaign.
     */
    public Mono<CampaignDto> duplicateCampaign(String campaignCode) {
        return campaignRepository.findByCampaignCode(campaignCode)
                .flatMap(original -> {
                    Campaign duplicate = Campaign.builder()
                            .userId(MVP_USER_ID)
                            .name(original.getName() + " (Copy)")
                            .templateId(original.getTemplateId())
                            .status(Campaign.CampaignStatus.DRAFT)
                            .audienceType(original.getAudienceType())
                            .audienceGroupId(original.getAudienceGroupId())
                            .recipientCount(original.getRecipientCount())
                            .scheduleType(Campaign.ScheduleType.SCHEDULED)
                            .build();

                    return campaignRepository.save(duplicate)
                            .flatMap(saved -> campaignRepository.findRecipientsByCampaignId(original.getId())
                                    .map(r -> CampaignRecipient.builder()
                                            .campaignId(saved.getId())
                                            .contactId(r.getContactId())
                                            .status(CampaignRecipient.MessageStatus.PENDING)
                                            .build())
                                    .collectList()
                                    .flatMap(recipients -> campaignRepository.saveRecipients(recipients)
                                            .then(Mono.just(saved))));
                })
                .flatMap(this::toDto)
                .switchIfEmpty(Mono.error(new NotFoundException("Campaign not found with code: " + campaignCode)));
    }

    /**
     * Delete a campaign and its associated data.
     */
    public Mono<Void> deleteCampaign(String campaignCode) {
        return campaignRepository.findByCampaignCode(campaignCode)
                .flatMap(campaign -> schedulerService.unscheduleCampaign(campaign.getId())
                        .then(campaignRepository.deleteById(campaign.getId()))
                        .onErrorResume(e -> {
                            log.error("Failed to unschedule campaign {} during delete", campaign.getId(), e);
                            return campaignRepository.deleteById(campaign.getId());
                        }))
                .switchIfEmpty(Mono.error(new NotFoundException("Campaign not found with code: " + campaignCode)));
    }

    private Mono<List<Long>> resolveContactIds(String audienceType, String audienceGroupId) {
        Campaign.AudienceType type;
        try {
            type = Campaign.AudienceType.valueOf(audienceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalArgumentException("Invalid audience type: " + audienceType));
        }

        return switch (type) {
            case ALL -> contactRepository.findAll(Pageable.unpaged())
                    .map(Contact::getId)
                    .collectList();
            case GROUP -> {
                if (audienceGroupId == null || audienceGroupId.isBlank()) {
                    yield Mono.error(new IllegalArgumentException("groupId is required for group audience"));
                }
                log.warn("Contact groups are not yet supported in MVP. Treating as empty.");
                yield Mono.just(List.<Long>of());
            }
            case SEGMENT -> {
                log.warn("Segments are not yet supported in MVP. Treating as empty.");
                yield Mono.just(List.<Long>of());
            }
        };
    }

    private Long parseGroupId(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(groupId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Mono<CampaignDto> toDto(Campaign campaign) {
        return templateRepository.findById(campaign.getTemplateId())
                .map(template -> new CampaignDto(
                        campaign.getId(),
                        campaign.getCampaignCode(),
                        campaign.getName(),
                        template.getName(),
                        campaign.getStatus() != null ? campaign.getStatus().name().toLowerCase() : null,
                        campaign.getAudienceType() != null ? campaign.getAudienceType().name().toLowerCase() : null,
                        campaign.getAudienceGroupId(),
                        campaign.getRecipientCount(),
                        campaign.getScheduleType() != null ? campaign.getScheduleType().name().toLowerCase() : null,
                        campaign.getScheduledDate(),
                        campaign.getScheduledTime(),
                        campaign.getScheduledAt(),
                        campaign.getSentAt(),
                        campaign.getEstimatedCost(),
                        campaign.getCreatedAt(),
                        campaign.getUpdatedAt(),
                        campaign.getCanceledAt(),
                        campaign.getCanceledReason()
                ))
                .switchIfEmpty(Mono.just(new CampaignDto(
                        campaign.getId(),
                        campaign.getCampaignCode(),
                        campaign.getName(),
                        null,
                        campaign.getStatus() != null ? campaign.getStatus().name().toLowerCase() : null,
                        campaign.getAudienceType() != null ? campaign.getAudienceType().name().toLowerCase() : null,
                        campaign.getAudienceGroupId(),
                        campaign.getRecipientCount(),
                        campaign.getScheduleType() != null ? campaign.getScheduleType().name().toLowerCase() : null,
                        campaign.getScheduledDate(),
                        campaign.getScheduledTime(),
                        campaign.getScheduledAt(),
                        campaign.getSentAt(),
                        campaign.getEstimatedCost(),
                        campaign.getCreatedAt(),
                        campaign.getUpdatedAt(),
                        campaign.getCanceledAt(),
                        campaign.getCanceledReason()
                )));
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
