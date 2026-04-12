package com.lewisrp.basemessages.backend.application.dto;

import java.util.List;

/**
 * DTO for paginated contacts response.
 */
public class ContactPageDto {
    private final List<ContactDto> data;
    private final long total;

    public ContactPageDto(List<ContactDto> data, long total) {
        this.data = data;
        this.total = total;
    }

    public List<ContactDto> getData() {
        return data;
    }

    public long getTotal() {
        return total;
    }
}
