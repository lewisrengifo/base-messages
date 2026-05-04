package com.lewisrp.basemessages.backend.adapter.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MetaApiClient {

    private final WebClient webClient;

    public MetaApiClient(@Value("${meta.api.base-url:https://graph.facebook.com/v25.0}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<SubmissionResult> submitTemplate(
            String wabaId,
            String accessToken,
            String name,
            String category,
            String language,
            String content,
            List<Map<String, String>> variables) {

        String url = String.format("/%s/message_templates", wabaId);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("category", category.toUpperCase());
        body.put("language", language);

        List<Map<String, Object>> components = new ArrayList<>();

        // BODY component (required)
        Map<String, Object> bodyComponent = new HashMap<>();
        bodyComponent.put("type", "BODY");
        bodyComponent.put("text", content);

        if (variables != null && !variables.isEmpty()) {
            List<String> exampleValues = variables.stream()
                    .map(v -> v.getOrDefault("example", v.getOrDefault("text", "")))
                    .toList();
            // Meta expects: example: { body_text: [ ["val1", "val2"] ] }
            Map<String, Object> example = new HashMap<>();
            example.put("body_text", List.of(exampleValues));
            bodyComponent.put("example", example);
            body.put("parameter_format", "POSITIONAL");
        }

        components.add(bodyComponent);
        body.put("components", components);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    log.info("Template submission successful: {}", response);
                    String metaId = response.get("id") != null ? String.valueOf(response.get("id")) : null;
                    return SubmissionResult.success(metaId, name);
                })
                .onErrorResume(error -> {
                    log.error("Template submission failed: {}", error.getMessage(), error);
                    String errorMessage = error.getMessage();
                    if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException webEx) {
                        String responseBody = webEx.getResponseBodyAsString();
                        log.error("Meta API error response: {}", responseBody);
                        errorMessage = responseBody;
                    }
                    return Mono.just(SubmissionResult.failure(errorMessage));
                });
    }

    public Mono<TemplateStatusResult> getTemplateStatus(String templateId, String wabaId, String accessToken) {
        String url = String.format("/%s", templateId);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("fields", "status")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object status = response.get("status");
                    String statusStr = status != null ? status.toString() : "UNKNOWN";
                    log.info("Meta template status for {}: {}", templateId, statusStr);
                    return new TemplateStatusResult(statusStr, null);
                })
                .onErrorResume(error -> {
                    log.error("Failed to get template status: {}", error.getMessage());
                    return Mono.just(new TemplateStatusResult("ERROR", null));
                });
    }

    public record TemplateStatusResult(
            String status,
            String rejectionReason
    ) {}

    public Mono<Boolean> testConnection(String wabaId, String phoneNumberId, String accessToken) {
        String targetId = (wabaId != null && !wabaId.isBlank()) ? wabaId : phoneNumberId;
        if (targetId == null || targetId.isBlank()) {
            log.error("Connection test failed: missing WABA ID or phone number ID");
            return Mono.just(false);
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + targetId)
                        .queryParam("fields", "id")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> response.containsKey("id"))
                .onErrorResume(error -> {
                    log.error("Connection test failed: {}", error.getMessage());
                    return Mono.just(false);
                });
    }

    public record SubmissionResult(
            boolean success,
            String metaTemplateId,
            String templateName,
            String errorMessage
    ) {
        public static SubmissionResult success(String metaTemplateId, String templateName) {
            return new SubmissionResult(true, metaTemplateId, templateName, null);
        }

        public static SubmissionResult failure(String errorMessage) {
            return new SubmissionResult(false, null, null, errorMessage);
        }
    }
}