package com.lewisrp.basemessages.backend.adapter.persistence;

import com.lewisrp.basemessages.backend.application.port.outbound.ConnectionRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Connection;
import com.lewisrp.basemessages.backend.adapter.persistence.entity.ConnectionEntity;
import com.lewisrp.basemessages.backend.adapter.persistence.mapper.ConnectionPersistenceMapper;
import com.lewisrp.basemessages.backend.adapter.persistence.repository.ConnectionR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Adapter implementing the ConnectionRepositoryPort using R2DBC.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionRepositoryAdapter implements ConnectionRepositoryPort {

    private final ConnectionR2dbcRepository connectionRepository;
    private final ConnectionPersistenceMapper connectionMapper;

    @Override
    public Mono<Connection> findCurrent() {
        return connectionRepository.findFirstByOrderByIdDesc()
                .map(connectionMapper::toDomain);
    }

    @Override
    public Mono<Connection> save(Connection connection) {
        ConnectionEntity entity = toEntity(connection);
        
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        
        return connectionRepository.save(entity)
                .map(connectionMapper::toDomain);
    }

    @Override
    public Mono<Boolean> exists() {
        return connectionRepository.existsBy();
    }

    @Override
    public Mono<Void> delete() {
        return connectionRepository.findFirstByOrderByIdDesc()
                .flatMap(connectionRepository::delete);
    }

    private ConnectionEntity toEntity(Connection connection) {
        ConnectionEntity entity = new ConnectionEntity();
        entity.setId(connection.getId());
        entity.setPhoneNumberId(connection.getPhoneNumberId());
        entity.setWabaId(connection.getWabaId());
        entity.setAccessToken(connection.getAccessToken());
        entity.setStatus(connection.getStatus() != null ? connection.getStatus().name() : Connection.ConnectionStatus.INACTIVE.name());
        entity.setLastHeartbeatAt(connection.getLastHeartbeatAt());
        entity.setCreatedAt(connection.getCreatedAt());
        entity.setUpdatedAt(connection.getUpdatedAt());
        return entity;
    }
}
