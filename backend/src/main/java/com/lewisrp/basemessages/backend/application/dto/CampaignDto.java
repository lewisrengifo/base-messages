package com.lewisrp.basemessages.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for campaign response.
 */
public class CampaignDto {

    private final Long id;
    private final String campaignCode;
    private final String name;
    private final String templateName;
    private final String status;
    private final String audienceType;
    private final Long audienceGroupId;
    private final Integer recipientCount;
    private final String scheduleType;
    private final LocalDate scheduledDate;
    private final LocalTime scheduledTime;
    private final LocalDateTime scheduledAt;
    private final LocalDateTime sentAt;
    private final BigDecimal estimatedCost;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime canceledAt;
    private final String canceledReason;

    public CampaignDto(Long id, String campaignCode, String name, String templateName,
                       String status, String audienceType, Long audienceGroupId, Integer recipientCount,
                       String scheduleType, LocalDate scheduledDate, LocalTime scheduledTime,
                       LocalDateTime scheduledAt, LocalDateTime sentAt, BigDecimal estimatedCost,
                       LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime canceledAt,
                       String canceledReason) {
        this.id = id;
        this.campaignCode = campaignCode;
        this.name = name;
        this.templateName = templateName;
        this.status = status;
        this.audienceType = audienceType;
        this.audienceGroupId = audienceGroupId;
        this.recipientCount = recipientCount;
        this.scheduleType = scheduleType;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
        this.estimatedCost = estimatedCost;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.canceledAt = canceledAt;
        this.canceledReason = canceledReason;
    }

    public Long getId() {
        return id;
    }

    public String getCampaignCode() {
        return campaignCode;
    }

    public String getName() {
        return name;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getStatus() {
        return status;
    }

    public String getAudienceType() {
        return audienceType;
    }

    public Long getAudienceGroupId() {
        return audienceGroupId;
    }

    public Integer getRecipientCount() {
        return recipientCount;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public String getCanceledReason() {
        return canceledReason;
    }
}
