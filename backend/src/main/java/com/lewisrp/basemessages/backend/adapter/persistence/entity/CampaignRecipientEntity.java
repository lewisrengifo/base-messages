package com.lewisrp.basemessages.backend.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * R2DBC entity for the campaign_recipients table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("campaign_recipients")
public class CampaignRecipientEntity {

    @Id
    private Long id;

    private Long campaignId;

    private Long contactId;

    private String status;

    private LocalDateTime sentAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime openedAt;

    private LocalDateTime clickedAt;

    private LocalDateTime failedAt;

    private String failureReason;

    private String deviceType;

    private LocalDateTime createdAt;
}
