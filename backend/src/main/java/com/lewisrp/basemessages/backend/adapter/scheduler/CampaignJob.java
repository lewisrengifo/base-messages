package com.lewisrp.basemessages.backend.adapter.scheduler;

import com.lewisrp.basemessages.backend.application.service.BroadcastService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

/**
 * Quartz job that executes a campaign broadcast.
 */
@Slf4j
public class CampaignJob implements Job {

    public static final String CAMPAIGN_ID_KEY = "campaignId";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long campaignId = context.getMergedJobDataMap().getLong(CAMPAIGN_ID_KEY);
        log.info("Quartz job executing for campaign: {}", campaignId);

        try {
            ApplicationContext appCtx = (ApplicationContext) context.getScheduler()
                    .getContext().get("applicationContext");
            BroadcastService broadcastService = appCtx.getBean(BroadcastService.class);
            broadcastService.executeCampaign(campaignId).block();
        } catch (Exception e) {
            log.error("Campaign broadcast failed for campaign: {}", campaignId, e);
            throw new JobExecutionException(e);
        }
    }
}
