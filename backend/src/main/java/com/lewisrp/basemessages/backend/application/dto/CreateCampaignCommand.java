package com.lewisrp.basemessages.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Command for creating a new campaign.
 */
public class CreateCampaignCommand {

    private final String name;
    private final Long templateId;
    private final String audienceType;
    private final String audienceGroupId;
    private final List<Long> contactIds;
    private final String scheduleType;
    private final LocalDateTime scheduledAt;

    public CreateCampaignCommand(String name, Long templateId, String audienceType,
                                 String audienceGroupId, List<Long> contactIds, String scheduleType, LocalDateTime scheduledAt) {
        this.name = name;
        this.templateId = templateId;
        this.audienceType = audienceType;
        this.audienceGroupId = audienceGroupId;
        this.contactIds = contactIds;
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

    public List<Long> getContactIds() {
        return contactIds;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }
}
