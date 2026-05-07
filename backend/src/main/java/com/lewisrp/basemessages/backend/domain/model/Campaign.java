package com.lewisrp.basemessages.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Campaign domain entity representing a broadcast message campaign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    private Long id;
    private Long userId;
    private String campaignCode;
    private String name;
    private Long templateId;
    private CampaignStatus status;
    private AudienceType audienceType;
    private Long audienceGroupId;
    private Integer recipientCount;
    private ScheduleType scheduleType;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private BigDecimal estimatedCost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime canceledAt;
    private String canceledReason;

    /**
     * Campaign lifecycle status.
     */
    public enum CampaignStatus {
        DRAFT, SCHEDULED, SENDING, SENT, CANCELED, FAILED
    }

    /**
     * Audience targeting type.
     */
    public enum AudienceType {
        ALL, GROUP, SEGMENT
    }

    /**
     * Scheduling mode.
     */
    public enum ScheduleType {
        IMMEDIATE, SCHEDULED
    }

    /**
     * Check if campaign can be scheduled.
     */
    public boolean canBeScheduled() {
        return status == CampaignStatus.DRAFT;
    }

    /**
     * Check if campaign can be canceled.
     */
    public boolean canBeCanceled() {
        return status == CampaignStatus.DRAFT || status == CampaignStatus.SCHEDULED;
    }

    /**
     * Check if campaign can be duplicated.
     */
    public boolean canBeDuplicated() {
        return true;
    }

    /**
     * Transition campaign to SCHEDULED status.
     */
    public void schedule(LocalDateTime scheduledAt) {
        if (!canBeScheduled()) {
            throw new IllegalStateException("Only DRAFT campaigns can be scheduled");
        }
        this.status = CampaignStatus.SCHEDULED;
        this.scheduledAt = scheduledAt;
    }

    /**
     * Transition campaign to SENDING status.
     */
    public void markSending() {
        if (status == CampaignStatus.SENDING) {
            return; // idempotent: already sending
        }
        if (status != CampaignStatus.DRAFT && status != CampaignStatus.SCHEDULED) {
            throw new IllegalStateException("Only DRAFT or SCHEDULED campaigns can start sending");
        }
        this.status = CampaignStatus.SENDING;
    }

    /**
     * Transition campaign to SENT status.
     */
    public void markSent() {
        if (status != CampaignStatus.SENDING) {
            throw new IllegalStateException("Only SENDING campaigns can be marked as sent");
        }
        this.status = CampaignStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Transition campaign to FAILED status.
     */
    public void markFailed() {
        if (status != CampaignStatus.SENDING && status != CampaignStatus.SCHEDULED) {
            throw new IllegalStateException("Only SENDING or SCHEDULED campaigns can be marked as failed");
        }
        this.status = CampaignStatus.FAILED;
    }

    /**
     * Cancel the campaign.
     */
    public void cancel(String reason) {
        if (!canBeCanceled()) {
            throw new IllegalStateException("Cannot cancel campaign in status: " + status);
        }
        this.status = CampaignStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
        this.canceledReason = reason;
    }
}
