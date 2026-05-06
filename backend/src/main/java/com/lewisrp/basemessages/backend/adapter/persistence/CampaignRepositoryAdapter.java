package com.lewisrp.basemessages.backend.adapter.persistence;

import com.lewisrp.basemessages.backend.adapter.persistence.entity.CampaignEntity;
import com.lewisrp.basemessages.backend.adapter.persistence.entity.CampaignRecipientEntity;
import com.lewisrp.basemessages.backend.adapter.persistence.mapper.CampaignPersistenceMapper;
import com.lewisrp.basemessages.backend.adapter.persistence.mapper.CampaignRecipientPersistenceMapper;
import com.lewisrp.basemessages.backend.adapter.persistence.repository.CampaignR2dbcRepository;
import com.lewisrp.basemessages.backend.adapter.persistence.repository.CampaignRecipientR2dbcRepository;
import com.lewisrp.basemessages.backend.application.port.outbound.CampaignRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Campaign;
import com.lewisrp.basemessages.backend.domain.model.CampaignRecipient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Adapter implementing the CampaignRepositoryPort using R2DBC.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignRepositoryAdapter implements CampaignRepositoryPort {

    private static final long MVP_USER_ID = 1L;

    private final CampaignR2dbcRepository campaignRepository;
    private final CampaignRecipientR2dbcRepository recipientRepository;
    private final CampaignPersistenceMapper campaignMapper;
    private final CampaignRecipientPersistenceMapper recipientMapper;
    private final DatabaseClient databaseClient;

    @Override
    public Flux<Campaign> findAll(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return campaignRepository.findAllByUserId(MVP_USER_ID)
                    .map(campaignMapper::toDomain);
        }
        return campaignRepository.findAllByUserId(MVP_USER_ID, pageable)
                .map(campaignMapper::toDomain);
    }

    @Override
    public Flux<Campaign> findByStatus(String status, Pageable pageable) {
        String normalizedStatus = status != null ? status.toLowerCase() : null;
        if (pageable == null || pageable.isUnpaged()) {
            return campaignRepository.findByUserIdAndStatus(MVP_USER_ID, normalizedStatus)
                    .map(campaignMapper::toDomain);
        }
        return campaignRepository.findByUserIdAndStatus(MVP_USER_ID, normalizedStatus, pageable)
                .map(campaignMapper::toDomain);
    }

    @Override
    public Mono<Campaign> findById(Long id) {
        return campaignRepository.findById(id)
                .filter(entity -> Long.valueOf(MVP_USER_ID).equals(entity.getUserId()))
                .map(campaignMapper::toDomain);
    }

    @Override
    public Mono<Campaign> findByCampaignCode(String campaignCode) {
        return campaignRepository.findByCampaignCode(campaignCode)
                .filter(entity -> Long.valueOf(MVP_USER_ID).equals(entity.getUserId()))
                .map(campaignMapper::toDomain);
    }

    @Override
    public Mono<Campaign> save(Campaign campaign) {
        CampaignEntity entity = campaignMapper.toEntity(campaign);
        if (entity.getUserId() == null) {
            entity.setUserId(MVP_USER_ID);
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        if (entity.getCampaignCode() == null || entity.getCampaignCode().isBlank()) {
            return generateCampaignCode()
                    .flatMap(code -> {
                        entity.setCampaignCode(code);
                        return insertCampaign(entity);
                    })
                    .map(campaignMapper::toDomain);
        }

        if (entity.getId() == null) {
            return insertCampaign(entity)
                    .map(campaignMapper::toDomain);
        }

        return updateCampaign(entity)
                .map(campaignMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return campaignRepository.deleteById(id);
    }

    @Override
    public Mono<Long> countAll() {
        return campaignRepository.countByUserId(MVP_USER_ID);
    }

    @Override
    public Mono<Long> countByStatus(String status) {
        String normalizedStatus = status != null ? status.toLowerCase() : null;
        return campaignRepository.countByUserIdAndStatus(MVP_USER_ID, normalizedStatus);
    }

    @Override
    public Flux<Campaign> search(String query, Pageable pageable) {
        return campaignRepository.searchByUserId(MVP_USER_ID, query, pageable)
                .map(campaignMapper::toDomain);
    }

    @Override
    public Mono<Long> countSearch(String query) {
        return campaignRepository.countSearchByUserId(MVP_USER_ID, query);
    }

    @Override
    public Flux<CampaignRecipient> findRecipientsByCampaignId(Long campaignId) {
        return recipientRepository.findByCampaignId(campaignId)
                .map(recipientMapper::toDomain);
    }

    @Override
    public Flux<CampaignRecipient> findRecipientsByCampaignIdAndStatus(Long campaignId, CampaignRecipient.MessageStatus status) {
        return recipientRepository.findByCampaignIdAndStatus(campaignId, status.name())
                .map(recipientMapper::toDomain);
    }

    @Override
    public Mono<Long> countRecipientsByCampaignIdAndStatus(Long campaignId, CampaignRecipient.MessageStatus status) {
        return recipientRepository.countByCampaignIdAndStatus(campaignId, status.name());
    }

    @Override
    public Mono<Void> saveRecipients(List<CampaignRecipient> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(recipients)
                .map(recipientMapper::toEntity)
                .flatMap(recipientRepository::save)
                .then();
    }

    @Override
    public Mono<Void> updateRecipientStatus(Long recipientId, CampaignRecipient.MessageStatus status, String failureReason) {
        LocalDateTime now = LocalDateTime.now();
        String timestampColumn = switch (status) {
            case SENT -> "sent_at";
            case DELIVERED -> "delivered_at";
            case OPENED -> "opened_at";
            case CLICKED -> "clicked_at";
            case FAILED -> "failed_at";
            default -> null;
        };

        StringBuilder sql = new StringBuilder("UPDATE campaign_recipients SET status = :status");
        if (failureReason != null) {
            sql.append(", failure_reason = :failureReason");
        }
        if (timestampColumn != null) {
            sql.append(", ").append(timestampColumn).append(" = :timestamp");
        }
        sql.append(" WHERE id = :id");

        org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("status", status.name())
                .bind("id", recipientId);

        if (failureReason != null) {
            spec = spec.bind("failureReason", failureReason);
        }
        if (timestampColumn != null) {
            spec = spec.bind("timestamp", now);
        }

        return spec.then();
    }

    private Mono<String> generateCampaignCode() {
        return campaignRepository.count()
                .map(count -> String.format("CAMP-%04d", count + 1));
    }

    private Mono<CampaignEntity> insertCampaign(CampaignEntity entity) {
        String sql = """
                INSERT INTO campaigns (user_id, campaign_code, name, template_id, status, audience_type, audience_group_id,
                                      recipient_count, schedule_type, scheduled_date, scheduled_time, scheduled_at, sent_at,
                                      estimated_cost, created_at, updated_at, canceled_at, canceled_reason)
                VALUES (:userId, :campaignCode, :name, :templateId, CAST(:status AS campaign_status),
                        CAST(:audienceType AS audience_type), :audienceGroupId, :recipientCount,
                        CAST(:scheduleType AS schedule_type), :scheduledDate, :scheduledTime, :scheduledAt, :sentAt,
                        :estimatedCost, :createdAt, :updatedAt, :canceledAt, :canceledReason)
                RETURNING id, user_id, campaign_code, name, template_id, status::text, audience_type::text,
                          audience_group_id, recipient_count, schedule_type::text, scheduled_date, scheduled_time,
                          scheduled_at, sent_at, estimated_cost, created_at, updated_at, canceled_at, canceled_reason
                """;

        return bindCampaignFields(databaseClient.sql(sql), entity)
                .map(this::mapCampaignRow)
                .one();
    }

    private Mono<CampaignEntity> updateCampaign(CampaignEntity entity) {
        String sql = """
                UPDATE campaigns SET
                    user_id = :userId,
                    campaign_code = :campaignCode,
                    name = :name,
                    template_id = :templateId,
                    status = CAST(:status AS campaign_status),
                    audience_type = CAST(:audienceType AS audience_type),
                    audience_group_id = :audienceGroupId,
                    recipient_count = :recipientCount,
                    schedule_type = CAST(:scheduleType AS schedule_type),
                    scheduled_date = :scheduledDate,
                    scheduled_time = :scheduledTime,
                    scheduled_at = :scheduledAt,
                    sent_at = :sentAt,
                    estimated_cost = :estimatedCost,
                    created_at = :createdAt,
                    updated_at = :updatedAt,
                    canceled_at = :canceledAt,
                    canceled_reason = :canceledReason
                WHERE id = :id
                RETURNING id, user_id, campaign_code, name, template_id, status::text, audience_type::text,
                          audience_group_id, recipient_count, schedule_type::text, scheduled_date, scheduled_time,
                          scheduled_at, sent_at, estimated_cost, created_at, updated_at, canceled_at, canceled_reason
                """;

        return bindCampaignFields(databaseClient.sql(sql), entity)
                .bind("id", entity.getId())
                .map(this::mapCampaignRow)
                .one();
    }

    private DatabaseClient.GenericExecuteSpec bindCampaignFields(DatabaseClient.GenericExecuteSpec spec, CampaignEntity entity) {
        spec = spec.bind("userId", entity.getUserId())
                .bind("campaignCode", entity.getCampaignCode())
                .bind("name", entity.getName())
                .bind("templateId", entity.getTemplateId())
                .bind("status", entity.getStatus())
                .bind("audienceType", entity.getAudienceType())
                .bind("recipientCount", entity.getRecipientCount())
                .bind("scheduleType", entity.getScheduleType())
                .bind("createdAt", entity.getCreatedAt())
                .bind("updatedAt", entity.getUpdatedAt());

        spec = bindNullable(spec, "audienceGroupId", entity.getAudienceGroupId(), Long.class);
        spec = bindNullable(spec, "scheduledDate", entity.getScheduledDate(), LocalDate.class);
        spec = bindNullable(spec, "scheduledTime", entity.getScheduledTime(), LocalTime.class);
        spec = bindNullable(spec, "scheduledAt", entity.getScheduledAt(), LocalDateTime.class);
        spec = bindNullable(spec, "sentAt", entity.getSentAt(), LocalDateTime.class);
        spec = bindNullable(spec, "estimatedCost", entity.getEstimatedCost(), BigDecimal.class);
        spec = bindNullable(spec, "canceledAt", entity.getCanceledAt(), LocalDateTime.class);
        spec = bindNullable(spec, "canceledReason", entity.getCanceledReason(), String.class);

        return spec;
    }

    private <T> DatabaseClient.GenericExecuteSpec bindNullable(DatabaseClient.GenericExecuteSpec spec, String name, T value, Class<T> type) {
        return value != null ? spec.bind(name, value) : spec.bindNull(name, type);
    }

    private CampaignEntity mapCampaignRow(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        CampaignEntity entity = new CampaignEntity();
        entity.setId(row.get("id", Long.class));
        entity.setUserId(row.get("user_id", Long.class));
        entity.setCampaignCode(row.get("campaign_code", String.class));
        entity.setName(row.get("name", String.class));
        entity.setTemplateId(row.get("template_id", Long.class));
        entity.setStatus(row.get("status", String.class));
        entity.setAudienceType(row.get("audience_type", String.class));
        entity.setAudienceGroupId(row.get("audience_group_id", Long.class));
        entity.setRecipientCount(row.get("recipient_count", Integer.class));
        entity.setScheduleType(row.get("schedule_type", String.class));
        entity.setScheduledDate(row.get("scheduled_date", LocalDate.class));
        entity.setScheduledTime(row.get("scheduled_time", LocalTime.class));
        entity.setScheduledAt(row.get("scheduled_at", LocalDateTime.class));
        entity.setSentAt(row.get("sent_at", LocalDateTime.class));
        entity.setEstimatedCost(row.get("estimated_cost", BigDecimal.class));
        entity.setCreatedAt(row.get("created_at", LocalDateTime.class));
        entity.setUpdatedAt(row.get("updated_at", LocalDateTime.class));
        entity.setCanceledAt(row.get("canceled_at", LocalDateTime.class));
        entity.setCanceledReason(row.get("canceled_reason", String.class));
        return entity;
    }
}
