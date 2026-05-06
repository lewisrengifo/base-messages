package com.lewisrp.basemessages.backend.adapter.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Service for scheduling and unscheduling campaign broadcasts using Quartz.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignSchedulerService {

    private final Scheduler scheduler;

    /**
     * Schedule a campaign broadcast at the specified time.
     */
    public Mono<Void> scheduleCampaign(Long campaignId, LocalDateTime scheduledAt) {
        return Mono.fromCallable(() -> {
            JobDataMap jobData = new JobDataMap();
            jobData.put(CampaignJob.CAMPAIGN_ID_KEY, campaignId);

            JobDetail jobDetail = JobBuilder.newJob(CampaignJob.class)
                    .withIdentity("campaignJob-" + campaignId, "campaigns")
                    .usingJobData(jobData)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("campaignTrigger-" + campaignId, "campaigns")
                    .startAt(Date.from(scheduledAt.atZone(ZoneId.systemDefault()).toInstant()))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled campaign {} at {}", campaignId, scheduledAt);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Unschedule a campaign broadcast.
     */
    public Mono<Void> unscheduleCampaign(Long campaignId) {
        return Mono.fromCallable(() -> {
            TriggerKey triggerKey = new TriggerKey("campaignTrigger-" + campaignId, "campaigns");
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
                log.info("Unscheduled campaign {}", campaignId);
            }
            JobKey jobKey = new JobKey("campaignJob-" + campaignId, "campaigns");
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
