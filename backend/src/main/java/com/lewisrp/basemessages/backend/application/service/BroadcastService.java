package com.lewisrp.basemessages.backend.application.service;

import com.lewisrp.basemessages.backend.adapter.external.MetaApiClient;
import com.lewisrp.basemessages.backend.adapter.security.EncryptionService;
import com.lewisrp.basemessages.backend.adapter.storage.StorageService;
import com.lewisrp.basemessages.backend.application.port.outbound.CampaignRepositoryPort;
import com.lewisrp.basemessages.backend.application.port.outbound.ConnectionRepositoryPort;
import com.lewisrp.basemessages.backend.application.port.outbound.ContactRepositoryPort;
import com.lewisrp.basemessages.backend.application.port.outbound.TemplateRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Campaign;
import com.lewisrp.basemessages.backend.domain.model.CampaignRecipient;
import com.lewisrp.basemessages.backend.domain.model.Connection;
import com.lewisrp.basemessages.backend.domain.model.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.util.List;

/**
 * Service responsible for executing broadcast campaigns.
 * Fetches pending recipients and sends messages via Meta API with concurrency control.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastService {

    private final CampaignRepositoryPort campaignRepository;
    private final TemplateRepositoryPort templateRepository;
    private final ContactRepositoryPort contactRepository;
    private final ConnectionRepositoryPort connectionRepository;
    private final MetaApiClient metaApiClient;
    private final EncryptionService encryptionService;
    private final StorageService storageService;

    /**
     * Execute a campaign by sending messages to all pending recipients.
     */
    public Mono<Void> executeCampaign(Long campaignId) {
        log.info("Starting broadcast for campaign: {}", campaignId);

        return campaignRepository.findById(campaignId)
                .flatMap(campaign -> {
                    if (campaign.getStatus() != Campaign.CampaignStatus.DRAFT
                            && campaign.getStatus() != Campaign.CampaignStatus.SCHEDULED
                            && campaign.getStatus() != Campaign.CampaignStatus.SENDING) {
                        return Mono.error(new IllegalStateException(
                                "Campaign cannot be executed in status: " + campaign.getStatus()));
                    }

                    if (campaign.getStatus() != Campaign.CampaignStatus.SENDING) {
                        campaign.markSending();
                        return campaignRepository.save(campaign)
                                .flatMap(savedCampaign -> Mono.zip(
                                        Mono.just(savedCampaign),
                                        templateRepository.findById(savedCampaign.getTemplateId()),
                                        connectionRepository.findCurrent()
                                ));
                    }

                    return Mono.zip(
                            Mono.just(campaign),
                            templateRepository.findById(campaign.getTemplateId()),
                            connectionRepository.findCurrent()
                    );
                })
                .flatMap(tuple -> {
                    Campaign campaign = tuple.getT1();
                    Template template = tuple.getT2();
                    Connection connection = tuple.getT3();

                    if (connection == null) {
                        return Mono.error(new IllegalStateException(
                                "No WhatsApp connection configured"));
                    }
                    if (!connection.isActive()) {
                        return Mono.error(new IllegalStateException(
                                "WhatsApp connection is not active"));
                    }

                    String accessToken = encryptionService.decrypt(connection.getAccessToken());
                    List<String> variableValues = extractVariableValues(template);
                    String languageCode = template.getLanguage() != null
                            ? template.getLanguage().name()
                            : "en_US";

                    // Generate fresh presigned URL for document header
                    String headerDocumentUrl = null;
                    if (template.getHeaderType() == Template.HeaderType.DOCUMENT
                            && template.getHeaderDocumentKey() != null) {
                        try {
                            headerDocumentUrl = storageService.generatePresignedUrl(template.getHeaderDocumentKey());
                        } catch (Exception e) {
                            log.error("Failed to generate presigned URL for campaign {}: {}", campaignId, e.getMessage());
                        }
                    }

                    return sendToAllRecipients(campaign, template, connection, accessToken, languageCode, variableValues, headerDocumentUrl);
                })
                .then(campaignRepository.findById(campaignId))
                .flatMap(campaign -> {
                    return campaignRepository.countRecipientsByCampaignIdAndStatus(campaignId, CampaignRecipient.MessageStatus.SENT)
                            .flatMap(sentCount -> {
                                if (sentCount > 0) {
                                    campaign.markSent();
                                } else {
                                    campaign.markFailed();
                                }
                                return campaignRepository.save(campaign).then();
                            });
                })
                .doOnSuccess(v -> log.info("Broadcast completed for campaign: {}", campaignId))
                .doOnError(e -> log.error("Broadcast failed for campaign: {}", campaignId, e));
    }

    private Mono<Void> sendToAllRecipients(Campaign campaign, Template template,
                                           Connection connection, String accessToken,
                                           String languageCode, List<String> variableValues,
                                           String headerDocumentUrl) {
        return campaignRepository.findRecipientsByCampaignIdAndStatus(campaign.getId(), CampaignRecipient.MessageStatus.PENDING)
                .flatMap(recipient ->
                                contactRepository.findById(recipient.getContactId())
                                        .flatMap(contact -> {
                                            log.debug("Sending message to {} for campaign {}", contact.getPhone(), campaign.getId());
                                            return metaApiClient.sendTemplateMessage(
                                                            connection.getPhoneNumberId(),
                                                            accessToken,
                                                            contact.getPhone(),
                                                            template.getName(),
                                                            languageCode,
                                                            variableValues,
                                                            headerDocumentUrl
                                                    )
                                                    .flatMap(result -> {
                                                        if (result.success()) {
                                                            return campaignRepository.updateRecipientStatus(
                                                                    recipient.getId(),
                                                                    CampaignRecipient.MessageStatus.SENT,
                                                                    null
                                                            );
                                                        } else {
                                                            log.warn("Failed to send to {}: {}", contact.getPhone(), result.errorMessage());
                                                            return campaignRepository.updateRecipientStatus(
                                                                    recipient.getId(),
                                                                    CampaignRecipient.MessageStatus.FAILED,
                                                                    result.errorMessage()
                                                            );
                                                        }
                                                    })
                                                    .onErrorResume(e -> {
                                                        log.error("Unexpected error sending to {}: {}", contact.getPhone(), e.getMessage());
                                                        return campaignRepository.updateRecipientStatus(
                                                                recipient.getId(),
                                                                CampaignRecipient.MessageStatus.FAILED,
                                                                e.getMessage()
                                                        );
                                                    });
                                        })
                                        .switchIfEmpty(Mono.defer(() -> {
                                            log.warn("Contact not found for recipient: {}", recipient.getId());
                                            return campaignRepository.updateRecipientStatus(
                                                    recipient.getId(),
                                                    CampaignRecipient.MessageStatus.FAILED,
                                                    "Contact not found"
                                            );
                                        })),
                        10 // max concurrency to respect Meta rate limits
                )
                .then();
    }

    private List<String> extractVariableValues(Template template) {
        if (template.getVariables() == null || template.getVariables().isEmpty()) {
            return List.of();
        }
        return template.getVariables().stream()
                .map(v -> v.getExample() != null ? v.getExample() : "")
                .toList();
    }
}
