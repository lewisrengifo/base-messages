package com.lewisrp.basemessages.backend.application.dto;

import java.time.LocalDateTime;

/**
 * Command for creating a new campaign.
 */
public class CreateCampaignCommand {

    private final String name;
    private final Long templateId;
    private final String audienceType;
    private final String audienceGroupId;
    private final String scheduleType;
    private final LocalDateTime scheduledAt;

    public CreateCampaignCommand(String name, Long templateId, String audienceType,
                                 String audienceGroupId, String scheduleType, LocalDateTime scheduledAt) {
        this.name = name;
        this.templateId = templateId;
        this.audienceType = audienceType;
        this.audienceGroupId = audienceGroupId;
        this.scheduleType = scheduleType;
        this.scheduledAt = scheduledAt;
    }

    public String getName() {
        return name;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public String getAudienceType() {
        return audienceType;
    }

    public String getAudienceGroupId() {
        return audienceGroupId;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }
}
