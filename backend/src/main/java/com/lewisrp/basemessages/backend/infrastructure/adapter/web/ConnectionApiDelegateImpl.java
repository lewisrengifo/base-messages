package com.lewisrp.basemessages.backend.infrastructure.adapter.web;

import com.lewisrp.basemessages.backend.application.dto.ConnectionCommand;
import com.lewisrp.basemessages.backend.application.dto.ConnectionStatusDto;
import com.lewisrp.basemessages.backend.application.service.ConnectionApplicationService;
import org.openapitools.api.ConnectionApiDelegate;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.model.ConnectionRequest;
import org.openapitools.model.ConnectionStatus;
import org.openapitools.model.ConnectionTestPost200Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;

/**
 * Implementation of the OpenAPI generated ConnectionApiDelegate interface.
 */
@Component
public class ConnectionApiDelegateImpl implements ConnectionApiDelegate {

    private final ConnectionApplicationService connectionService;

    public ConnectionApiDelegateImpl(ConnectionApplicationService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public Mono<ResponseEntity<ConnectionStatus>> connectionGet(ServerWebExchange exchange) {
        return connectionService.getConnectionStatus()
                .map(this::toApiModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(new ConnectionStatus()));
    }

    @Override
    public Mono<ResponseEntity<ConnectionStatus>> connectionPost(Mono<ConnectionRequest> connectionRequest, ServerWebExchange exchange) {
        return connectionRequest
                .map(req -> new ConnectionCommand(
                        req.getPhoneNumberId(),
                        req.getWabaId(),
                        req.getAccessToken()
                ))
                .flatMap(connectionService::saveConnection)
                .map(this::toApiModel)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> connectionDelete(ServerWebExchange exchange) {
        return connectionService.deleteConnection()
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<ConnectionTestPost200Response>> connectionTestPost(ServerWebExchange exchange) {
        return connectionService.testConnection()
                .map(success -> {
                    ConnectionTestPost200Response response = new ConnectionTestPost200Response();
                    response.setSuccess(success);
                    response.setMessage(success ? "Connection successful" : "Connection failed");
                    response.setLatency(0);
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.ok(createTestResponse(false, "No connection configured", 0)));
    }

    private ConnectionTestPost200Response createTestResponse(boolean success, String message, int latency) {
        ConnectionTestPost200Response response = new ConnectionTestPost200Response();
        response.setSuccess(success);
        response.setMessage(message);
        response.setLatency(latency);
        return response;
    }

    private ConnectionStatus toApiModel(ConnectionStatusDto dto) {
        ConnectionStatus status = new ConnectionStatus();
        
        // Convert string status to enum
        if (dto.getStatus() != null) {
            try {
                status.setStatus(ConnectionStatus.StatusEnum.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                status.setStatus(ConnectionStatus.StatusEnum.INACTIVE);
            }
        } else {
            status.setStatus(ConnectionStatus.StatusEnum.INACTIVE);
        }
        
        status.setPhoneNumberId(dto.getPhoneNumberId());
        status.setWabaId(dto.getWabaId());
        
        // Convert LocalDateTime to OffsetDateTime wrapped in JsonNullable
        if (dto.getLastHeartbeatAt() != null) {
            status.setLastHeartbeat(JsonNullable.of(dto.getLastHeartbeatAt().atOffset(ZoneOffset.UTC)));
        }
        
        // Convert endpoint connectivity to enum
        if ("connected".equalsIgnoreCase(dto.getEndpointConnectivity())) {
            status.setEndpointConnectivity(ConnectionStatus.EndpointConnectivityEnum.CONNECTED);
        } else {
            status.setEndpointConnectivity(ConnectionStatus.EndpointConnectivityEnum.FAILED);
        }
        
        return status;
    }
}