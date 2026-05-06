package com.lewisrp.basemessages.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Campaign recipient domain entity representing an individual message delivery.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRecipient {

    private Long id;
    private Long campaignId;
    private Long contactId;
    private MessageStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime openedAt;
    private LocalDateTime clickedAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private String deviceType;
    private LocalDateTime createdAt;

    /**
     * Message delivery status.
     */
    public enum MessageStatus {
        PENDING, SENT, DELIVERED, OPENED, CLICKED, BOUNCED, FAILED
    }

    /**
     * Mark recipient as sent.
     */
    public void markSent() {
        this.status = MessageStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Mark recipient as failed.
     */
    public void markFailed(String reason) {
        this.status = MessageStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
    }
}
