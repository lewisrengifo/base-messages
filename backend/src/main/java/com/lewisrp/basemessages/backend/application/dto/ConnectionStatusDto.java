package com.lewisrp.basemessages.backend.application.dto;

import java.time.LocalDateTime;

/**
 * DTO for connection status response.
 */
public class ConnectionStatusDto {
    private final String status;
    private final String phoneNumberId;
    private final String wabaId;
    private final LocalDateTime lastHeartbeatAt;
    private final String endpointConnectivity;

    public ConnectionStatusDto(String status, String phoneNumberId, String wabaId, 
                               LocalDateTime lastHeartbeatAt, String endpointConnectivity) {
        this.status = status;
        this.phoneNumberId = phoneNumberId;
        this.wabaId = wabaId;
        this.lastHeartbeatAt = lastHeartbeatAt;
        this.endpointConnectivity = endpointConnectivity;
    }

    public String getStatus() {
        return status;
    }

    public String getPhoneNumberId() {
        return phoneNumberId;
    }

    public String getWabaId() {
        return wabaId;
    }

    public LocalDateTime getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public String getEndpointConnectivity() {
        return endpointConnectivity;
    }
}
