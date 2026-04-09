package com.lewisrp.basemessages.backend.infrastructure.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * R2DBC entity for the connections table (WhatsApp API credentials).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("connections")
public class ConnectionEntity {

    @Id
    private Long id;

    private String phoneNumberId;

    private String wabaId;

    private String accessToken;

    private String status;

    private LocalDateTime lastHeartbeatAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
