package com.lewisrp.basemessages.backend.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * R2DBC entity for the campaigns table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("campaigns")
public class CampaignEntity {

    @Id
    private Long id;

    private Long userId;

    private String campaignCode;

    private String name;

    private Long templateId;

    private String status;

    private String audienceType;

    private Long audienceGroupId;

    private Integer recipientCount;

    private String scheduleType;

    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    private BigDecimal estimatedCost;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime canceledAt;

    private String canceledReason;
}
