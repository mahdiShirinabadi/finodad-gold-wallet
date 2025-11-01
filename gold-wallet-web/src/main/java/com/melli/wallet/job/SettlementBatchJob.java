package com.melli.wallet.job;

import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.service.JobService;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Class Name: SettlementBatchJob
 * Author: Mahdi Shirinabadi
 * Date: 11/1/2025
 */
@Log4j2
@Component
@RequiredArgsConstructor
@Profile({"prod", "staging","dev"})
public class SettlementBatchJob {

    private final JobService jobService;

    @Scheduled(cron = "${batchInsertJob.cron:0 0 2 * * ?}") // Default: run at 2 AM daily
    @Async("threadPoolExecutorForJob")
    public void batchSettlement() {

        ThreadContext.put("uuid", UUID.randomUUID().toString().toUpperCase().replace("-", ""));

        try {
            log.info("Starting batchSettlement");
            jobService.batchSettlement();
            log.info("Terminate batchSettlement");
        } catch (Exception e) {
            log.error("Error in batchSettlement", e);
        } finally {
            ThreadContext.clearAll();
        }
    }
}
