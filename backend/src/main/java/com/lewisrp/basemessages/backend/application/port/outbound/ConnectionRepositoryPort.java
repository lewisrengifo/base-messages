package com.lewisrp.basemessages.backend.application.port.outbound;

import com.lewisrp.basemessages.backend.domain.model.Connection;
import reactor.core.publisher.Mono;

/**
 * Outbound port for connection persistence operations.
 */
public interface ConnectionRepositoryPort {

    /**
     * Find the current connection (MVP supports single connection).
     */
    Mono<Connection> findCurrent();

    /**
     * Save a connection.
     */
    Mono<Connection> save(Connection connection);

    /**
     * Check if any connection exists.
     */
    Mono<Boolean> exists();

    /**
     * Delete the current connection.
     */
    Mono<Void> delete();
}
