package com.lewisrp.basemessages.backend.application.service;

import com.lewisrp.basemessages.backend.application.dto.ConnectionCommand;
import com.lewisrp.basemessages.backend.application.dto.ConnectionStatusDto;
import com.lewisrp.basemessages.backend.application.port.outbound.ConnectionRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Connection;
import com.lewisrp.basemessages.backend.adapter.external.MetaApiClient;
import com.lewisrp.basemessages.backend.adapter.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Application service for connection management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionApplicationService {

    private final ConnectionRepositoryPort connectionRepository;
    private final EncryptionService encryptionService;
    private final MetaApiClient metaApiClient;

    /**
     * Get current connection status.
     */
    public Mono<ConnectionStatusDto> getConnectionStatus() {
        return connectionRepository.findCurrent()
                .map(this::toStatusDto)
                .defaultIfEmpty(new ConnectionStatusDto("INACTIVE", null, null, null, null));
    }

    /**
     * Create or update connection.
     */
    public Mono<ConnectionStatusDto> saveConnection(ConnectionCommand command) {
        log.info("Saving connection for phone number: {}", command.getPhoneNumberId());

        // Encrypt access token
        String encryptedToken = encryptionService.encrypt(command.getAccessToken());

        // Build connection model
        Connection connection = Connection.builder()
                .phoneNumberId(command.getPhoneNumberId())
                .wabaId(command.getWabaId())
                .accessToken(encryptedToken)
                .status(Connection.ConnectionStatus.INACTIVE)
                .build();

        // Test connection with Meta
        return metaApiClient.testConnection(command.getAccessToken())
                .flatMap(isValid -> {
                    if (isValid) {
                        connection.setStatus(Connection.ConnectionStatus.ACTIVE);
                        connection.updateHeartbeat();
                    } else {
                        connection.setStatus(Connection.ConnectionStatus.ERROR);
                    }
                    
                    return connectionRepository.save(connection);
                })
                .map(this::toStatusDto);
    }

    /**
     * Test current connection.
     */
    public Mono<Boolean> testConnection() {
        return connectionRepository.findCurrent()
                .flatMap(conn -> {
                    String decryptedToken = encryptionService.decrypt(conn.getAccessToken());
                    return metaApiClient.testConnection(decryptedToken);
                })
                .defaultIfEmpty(false);
    }

    /**
     * Delete connection.
     */
    public Mono<Void> deleteConnection() {
        return connectionRepository.delete();
    }

    /**
     * Convert domain model to DTO.
     */
    private ConnectionStatusDto toStatusDto(Connection connection) {
        return new ConnectionStatusDto(
                connection.getStatus() != null ? connection.getStatus().name() : "INACTIVE",
                connection.getMaskedPhoneNumberId(),
                connection.getMaskedWabaId(),
                connection.getLastHeartbeatAt(),
                connection.isActive() ? "connected" : "failed"
        );
    }
}
