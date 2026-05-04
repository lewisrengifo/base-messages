package com.lewisrp.basemessages.backend.adapter.web;

import com.lewisrp.basemessages.backend.application.exception.MetaSubmissionException;
import com.lewisrp.basemessages.backend.application.service.TemplateApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Global exception handler for the REST API.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MetaSubmissionException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleMetaSubmissionException(
            MetaSubmissionException ex, ServerWebExchange exchange) {
        log.warn("Meta submission error: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "code", "META_SUBMISSION_FAILED",
                "message", ex.getMessage(),
                "details", Map.of("metaError", ex.getMetaErrorJson())
        );

        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgumentException(
            IllegalArgumentException ex, ServerWebExchange exchange) {
        log.warn("Bad request: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "code", "INVALID_REQUEST",
                "message", ex.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalStateException(
            IllegalStateException ex, ServerWebExchange exchange) {
        log.warn("Illegal state: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "code", "INVALID_STATE",
                "message", ex.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    @ExceptionHandler(TemplateApplicationService.NotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNotFoundException(
            TemplateApplicationService.NotFoundException ex, ServerWebExchange exchange) {
        log.warn("Not found: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "code", "NOT_FOUND",
                "message", ex.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> body = Map.of(
                "code", "INTERNAL_ERROR",
                "message", "An unexpected error occurred"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }
}
