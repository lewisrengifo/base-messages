package com.lewisrp.basemessages.backend.adapter.scheduler;

import com.lewisrp.basemessages.backend.application.port.outbound.CampaignRepositoryPort;
import com.lewisrp.basemessages.backend.domain.model.Campaign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Startup runner that recovers scheduled campaigns after application restart.
 * Re-schedules any campaigns in SCHEDULED status with a future scheduledAt time.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignSchedulerStartup implements ApplicationRunner {

    private final CampaignRepositoryPort campaignRepository;
    private final CampaignSchedulerService schedulerService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Recovering scheduled campaigns...");

        campaignRepository.findByStatus(Campaign.CampaignStatus.SCHEDULED.name().toLowerCase(), Pageable.unpaged())
                .flatMap(campaign -> {
                    if (campaign.getScheduledAt() != null && campaign.getScheduledAt().isAfter(LocalDateTime.now())) {
                        return schedulerService.scheduleCampaign(campaign.getId(), campaign.getScheduledAt())
                                .thenReturn(campaign)
                                .doOnSuccess(c -> log.info("Rescheduled campaign {} at {}", c.getId(), c.getScheduledAt()))
                                .onErrorResume(e -> {
                                    log.error("Failed to reschedule campaign {}", campaign.getId(), e);
                                    return reactor.core.publisher.Mono.empty();
                                });
                    } else {
                        log.warn("Campaign {} is past due, marking as failed", campaign.getId());
                        campaign.markFailed();
                        return campaignRepository.save(campaign).then(reactor.core.publisher.Mono.empty());
                    }
                })
                .collectList()
                .doOnSuccess(list -> log.info("Recovered {} scheduled campaigns", list.size()))
                .block();
    }
}
