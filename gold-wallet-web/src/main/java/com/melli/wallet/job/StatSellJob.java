package com.melli.wallet.job;

import com.melli.wallet.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile({"prod", "staging"})
public class StatSellJob {

    private final JobService jobService;

    @Scheduled(cron = "${statSellJob.cron:0 5 2 * * ?}") // Default: run at 2:05 AM daily
    @Async("threadPoolExecutorForJob")
    public void generateSellStatistics() {
        ThreadContext.put("uuid", UUID.randomUUID().toString().toUpperCase().replace("-", ""));

        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formattedDate = oneDayAgo.format(formatter);

        try {
            log.info("Starting sell statistics job for date: {}", formattedDate);
            
            
            jobService.generateSellStatistics(formattedDate, formattedDate, "JOB");
            log.info("Sell statistics job completed successfully for date: {}", formattedDate);
        } catch (Exception e) {
            log.error("Error in sell statistics job for date: {}", formattedDate, e);
        } finally {
            ThreadContext.clearAll();
        }
    }
}
