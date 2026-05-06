package com.lewisrp.basemessages.backend.application.port.outbound;

import com.lewisrp.basemessages.backend.domain.model.Campaign;
import com.lewisrp.basemessages.backend.domain.model.CampaignRecipient;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Outbound port for campaign persistence operations.
 */
public interface CampaignRepositoryPort {

    Flux<Campaign> findAll(Pageable pageable);

    Flux<Campaign> findByStatus(String status, Pageable pageable);

    Mono<Campaign> findById(Long id);

    Mono<Campaign> findByCampaignCode(String campaignCode);

    Mono<Campaign> save(Campaign campaign);

    Mono<Void> deleteById(Long id);

    Mono<Long> countAll();

    Mono<Long> countByStatus(String status);

    Flux<Campaign> search(String query, Pageable pageable);

    Mono<Long> countSearch(String query);

    Flux<CampaignRecipient> findRecipientsByCampaignId(Long campaignId);

    Flux<CampaignRecipient> findRecipientsByCampaignIdAndStatus(Long campaignId, CampaignRecipient.MessageStatus status);

    Mono<Long> countRecipientsByCampaignIdAndStatus(Long campaignId, CampaignRecipient.MessageStatus status);

    Mono<Void> saveRecipients(List<CampaignRecipient> recipients);

    Mono<Void> updateRecipientStatus(Long recipientId, CampaignRecipient.MessageStatus status, String failureReason);
}
