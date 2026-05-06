package com.lewisrp.basemessages.backend.adapter.persistence.repository;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.CampaignRecipientEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for CampaignRecipientEntity.
 */
@Repository
public interface CampaignRecipientR2dbcRepository extends ReactiveCrudRepository<CampaignRecipientEntity, Long> {

    Flux<CampaignRecipientEntity> findByCampaignId(Long campaignId);

    @Query("""
            SELECT * FROM campaign_recipients
            WHERE campaign_id = :campaignId
              AND status = CAST(LOWER(:status) AS message_status)
            """)
    Flux<CampaignRecipientEntity> findByCampaignIdAndStatus(Long campaignId, String status);

    @Query("""
            SELECT COUNT(*) FROM campaign_recipients
            WHERE campaign_id = :campaignId
              AND status = CAST(LOWER(:status) AS message_status)
            """)
    Mono<Long> countByCampaignIdAndStatus(Long campaignId, String status);
}
