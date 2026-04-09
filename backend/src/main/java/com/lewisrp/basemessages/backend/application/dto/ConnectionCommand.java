package com.lewisrp.basemessages.backend.application.dto;

/**
 * DTO for creating/updating connection.
 */
public class ConnectionCommand {
    private final String phoneNumberId;
    private final String wabaId;
    private final String accessToken;

    public ConnectionCommand(String phoneNumberId, String wabaId, String accessToken) {
        this.phoneNumberId = phoneNumberId;
        this.wabaId = wabaId;
        this.accessToken = accessToken;
    }

    public String getPhoneNumberId() {
        return phoneNumberId;
    }

    public String getWabaId() {
        return wabaId;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
