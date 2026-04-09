package com.lewisrp.basemessages.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Connection domain entity representing WhatsApp Business API credentials.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Connection {

    private Long id;
    private String phoneNumberId;
    private String wabaId;
    private String accessToken; // Encrypted
    private ConnectionStatus status;
    private LocalDateTime lastHeartbeatAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Connection status.
     */
    public enum ConnectionStatus {
        ACTIVE, INACTIVE, ERROR
    }

    /**
     * Check if connection is active and ready to use.
     */
    public boolean isActive() {
        return status == ConnectionStatus.ACTIVE;
    }

    /**
     * Update last heartbeat timestamp.
     */
    public void updateHeartbeat() {
        this.lastHeartbeatAt = LocalDateTime.now();
    }

    /**
     * Get masked phone number ID for security (show only last 4 digits).
     */
    public String getMaskedPhoneNumberId() {
        if (phoneNumberId == null || phoneNumberId.length() <= 4) {
            return "****";
        }
        return "****" + phoneNumberId.substring(phoneNumberId.length() - 4);
    }

    /**
     * Get masked WABA ID for security (show only last 4 digits).
     */
    public String getMaskedWabaId() {
        if (wabaId == null || wabaId.length() <= 4) {
            return "****";
        }
        return "****" + wabaId.substring(wabaId.length() - 4);
    }
}
