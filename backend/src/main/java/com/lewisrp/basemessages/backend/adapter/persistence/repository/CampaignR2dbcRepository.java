package com.lewisrp.basemessages.backend.adapter.persistence.repository;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.CampaignEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for CampaignEntity.
 */
@Repository
public interface CampaignR2dbcRepository extends ReactiveCrudRepository<CampaignEntity, Long> {

    Flux<CampaignEntity> findAllByUserId(Long userId, Pageable pageable);

    @Query("""
            SELECT * FROM campaigns
            WHERE user_id = :userId
            ORDER BY id DESC
            """)
    Flux<CampaignEntity> findAllByUserId(Long userId);

    @Query("""
            SELECT * FROM campaigns
            WHERE user_id = :userId
              AND status = CAST(LOWER(:status) AS campaign_status)
            ORDER BY id DESC
            LIMIT :#{#pageable.pageSize}
            OFFSET :#{#pageable.offset}
            """)
    Flux<CampaignEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    @Query("""
            SELECT * FROM campaigns
            WHERE user_id = :userId
              AND status = CAST(LOWER(:status) AS campaign_status)
            ORDER BY id DESC
            """)
    Flux<CampaignEntity> findByUserIdAndStatus(Long userId, String status);

    Mono<CampaignEntity> findByCampaignCode(String campaignCode);

    Mono<Long> countByUserId(Long userId);

    @Query("""
            SELECT COUNT(*) FROM campaigns
            WHERE user_id = :userId
              AND status = CAST(LOWER(:status) AS campaign_status)
            """)
    Mono<Long> countByUserIdAndStatus(Long userId, String status);

    @Query("""
            SELECT * FROM campaigns
            WHERE user_id = :userId
              AND LOWER(name) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY id DESC
            LIMIT :#{#pageable.pageSize}
            OFFSET :#{#pageable.offset}
            """)
    Flux<CampaignEntity> searchByUserId(Long userId, String query, Pageable pageable);

    @Query("""
            SELECT COUNT(*) FROM campaigns
            WHERE user_id = :userId
              AND LOWER(name) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Mono<Long> countSearchByUserId(Long userId, String query);
}
