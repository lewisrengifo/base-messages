package com.lewisrp.basemessages.backend.adapter.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
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
    private final String baseUrl;

    public MetaApiClient(@Value("${meta.api.base-url:https://graph.facebook.com/v25.0}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Upload media to Meta using the Resumable Upload API to get a header handle.
     */
    public Mono<String> uploadMedia(String appId, String accessToken, byte[] fileBytes, String fileName, String mimeType) {
        // Step 1: Create upload session
        String sessionUrl = String.format("/%s/uploads", appId);
        Map<String, Object> sessionBody = new HashMap<>();
        sessionBody.put("file_length", fileBytes.length);
        sessionBody.put("file_type", mimeType);
        sessionBody.put("file_name", fileName);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(sessionUrl)
                        .queryParam("access_token", accessToken)
                        .build())
                .bodyValue(sessionBody)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(sessionResponse -> {
                    Object uploadId = sessionResponse.get("id");
                    if (uploadId == null) {
                        return Mono.error(new RuntimeException("No upload session ID returned from Meta"));
                    }
                    String uploadUrl = String.format("%s/%s", baseUrl, uploadId);

                    // Step 2: Upload file data
                    return webClient.post()
                            .uri(uploadUrl)
                            .header(HttpHeaders.AUTHORIZATION, "OAuth " + accessToken)
                            .header("file_offset", "0")
                            .header(HttpHeaders.CONTENT_TYPE, mimeType)
                            .bodyValue(fileBytes)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(uploadResponse -> {
                                Object handle = uploadResponse.get("h");
                                if (handle == null) {
                                    throw new RuntimeException("No handle returned from Meta upload");
                                }
                                log.info("Media uploaded to Meta successfully, handle: {}", handle);
                                return handle.toString();
                            });
                })
                .onErrorResume(error -> {
                    log.error("Media upload to Meta failed: {}", error.getMessage(), error);
                    return Mono.error(new RuntimeException("Failed to upload media to Meta: " + error.getMessage()));
                });
    }

    public Mono<SubmissionResult> submitTemplate(
            String wabaId,
            String accessToken,
            String name,
            String category,
            String language,
            String content,
            List<Map<String, String>> variables,
            String headerType,
            String headerHandle) {

        String url = String.format("/%s/message_templates", wabaId);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("category", category.toUpperCase());
        body.put("language", language);

        List<Map<String, Object>> components = new ArrayList<>();

        // HEADER component (optional)
        if ("DOCUMENT".equalsIgnoreCase(headerType) && headerHandle != null) {
            Map<String, Object> headerComponent = new HashMap<>();
            headerComponent.put("type", "HEADER");
            headerComponent.put("format", "DOCUMENT");
            Map<String, Object> example = new HashMap<>();
            example.put("header_handle", List.of(headerHandle));
            headerComponent.put("example", example);
            components.add(headerComponent);
        }

        // BODY component (required)
        Map<String, Object> bodyComponent = new HashMap<>();
        bodyComponent.put("type", "BODY");
        bodyComponent.put("text", content);

        if (variables != null && !variables.isEmpty()) {
            List<String> exampleValues = variables.stream()
                    .map(v -> v.getOrDefault("example", v.getOrDefault("text", "")))
                    .toList();
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

    public Mono<SendResult> sendTemplateMessage(
            String phoneNumberId,
            String accessToken,
            String toPhone,
            String templateName,
            String languageCode,
            List<String> variableValues,
            String headerDocumentUrl) {

        String url = String.format("/%s/messages", phoneNumberId);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", toPhone);
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", templateName);
        Map<String, String> language = new HashMap<>();
        language.put("code", languageCode);
        template.put("language", language);

        List<Map<String, Object>> components = new ArrayList<>();

        // Header component with document
        if (headerDocumentUrl != null && !headerDocumentUrl.isBlank()) {
            Map<String, Object> headerParam = new HashMap<>();
            headerParam.put("type", "document");
            Map<String, String> document = new HashMap<>();
            document.put("link", headerDocumentUrl);
            headerParam.put("document", document);

            Map<String, Object> headerComponent = new HashMap<>();
            headerComponent.put("type", "header");
            headerComponent.put("parameters", List.of(headerParam));
            components.add(headerComponent);
        }

        // Body component with variables
        if (variableValues != null && !variableValues.isEmpty()) {
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (String value : variableValues) {
                Map<String, Object> param = new HashMap<>();
                param.put("type", "text");
                param.put("text", value);
                parameters.add(param);
            }
            Map<String, Object> component = new HashMap<>();
            component.put("type", "body");
            component.put("parameters", parameters);
            components.add(component);
        }

        if (!components.isEmpty()) {
            template.put("components", components);
        }

        body.put("template", template);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    log.info("Message sent successfully: {}", response);
                    String metaMessageId = null;
                    if (response.get("messages") instanceof List messages && !messages.isEmpty()) {
                        Object first = messages.get(0);
                        if (first instanceof Map msgMap) {
                            metaMessageId = msgMap.get("id") != null ? String.valueOf(msgMap.get("id")) : null;
                        }
                    }
                    return SendResult.success(metaMessageId);
                })
                .onErrorResume(error -> {
                    log.error("Message sending failed: {}", error.getMessage(), error);
                    String errorMessage = error.getMessage();
                    if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException webEx) {
                        String responseBody = webEx.getResponseBodyAsString();
                        log.error("Meta API error response: {}", responseBody);
                        errorMessage = responseBody;
                    }
                    return Mono.just(SendResult.failure(errorMessage));
                });
    }

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

    public record SendResult(
            boolean success,
            String metaMessageId,
            String errorMessage
    ) {
        public static SendResult success(String metaMessageId) {
            return new SendResult(true, metaMessageId, null);
        }

        public static SendResult failure(String errorMessage) {
            return new SendResult(false, null, errorMessage);
        }
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

    public record TemplateStatusResult(
            String status,
            String rejectionReason
    ) {}
}
