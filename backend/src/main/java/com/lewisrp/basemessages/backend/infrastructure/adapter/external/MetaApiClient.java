package com.lewisrp.basemessages.backend.infrastructure.adapter.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Client for Meta Graph API to submit templates and send messages.
 */
@Component
@Slf4j
public class MetaApiClient {

    private final WebClient webClient;

    @Value("${meta.api.base-url:https://graph.facebook.com/v18.0}")
    private String baseUrl;

    public MetaApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Submit a template to Meta for approval.
     *
     * @param wabaId WhatsApp Business Account ID
     * @param accessToken Access token for authentication
     * @param name Template name
     * @param category Template category (MARKETING, UTILITY, AUTHENTICATION)
     * @param language Template language code
     * @param content Template content with {{variables}}
     * @param variables List of variable examples
     * @return Template submission response with ID
     */
    public Mono<SubmissionResult> submitTemplate(
            String wabaId,
            String accessToken,
            String name,
            String category,
            String language,
            String content,
            List<Map<String, String>> variables) {

        String url = String.format("/%s/message_templates", wabaId);

        // Build components array for the template
        Map<String, Object> body = Map.of(
                "name", name,
                "category", category.toUpperCase(),
                "language", language,
                "components", List.of(Map.of(
                        "type", "BODY",
                        "text", content
                ))
        );

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("access_token", accessToken)
                        .build())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    log.info("Template submission successful: {}", response);
                    return SubmissionResult.success(
                            String.valueOf(response.get("id")),
                            name
                    );
                })
                .onErrorResume(error -> {
                    log.error("Template submission failed: {}", error.getMessage(), error);
                    return Mono.just(SubmissionResult.failure(error.getMessage()));
                });
    }

    /**
     * Check template status by ID.
     *
     * @param templateId Meta template ID
     * @param wabaId WhatsApp Business Account ID
     * @param accessToken Access token
     * @return Template status
     */
    public Mono<String> getTemplateStatus(String templateId, String wabaId, String accessToken) {
        String url = String.format("/%s/message_templates", wabaId);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // Parse status from response
                    return "PENDING"; // Simplified for MVP
                })
                .onErrorResume(error -> {
                    log.error("Failed to get template status: {}", error.getMessage());
                    return Mono.just("ERROR");
                });
    }

    /**
     * Test connection to Meta API.
     *
     * @param accessToken Access token
     * @return true if connection is valid
     */
    public Mono<Boolean> testConnection(String accessToken) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me")
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> response.containsKey("id"))
                .onErrorResume(error -> {
                    log.error("Connection test failed: {}", error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * Result of template submission.
     */
    public record SubmissionResult(
            boolean success,
            String templateId,
            String templateName,
            String errorMessage
    ) {
        public static SubmissionResult success(String templateId, String templateName) {
            return new SubmissionResult(true, templateId, templateName, null);
        }

        public static SubmissionResult failure(String errorMessage) {
            return new SubmissionResult(false, null, null, errorMessage);
        }
    }
}
