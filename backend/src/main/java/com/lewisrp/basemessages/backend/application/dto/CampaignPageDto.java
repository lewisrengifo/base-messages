package com.lewisrp.basemessages.backend.application.dto;

import java.util.List;

/**
 * DTO for paginated campaign list response.
 */
public class CampaignPageDto {

    private final List<CampaignDto> data;
    private final long total;

    public CampaignPageDto(List<CampaignDto> data, long total) {
        this.data = data;
        this.total = total;
    }

    public List<CampaignDto> getData() {
        return data;
    }

    public long getTotal() {
        return total;
    }
}
