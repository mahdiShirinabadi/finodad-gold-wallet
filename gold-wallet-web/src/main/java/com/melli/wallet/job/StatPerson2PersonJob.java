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
@Profile({"prod", "staging","dev"})
public class StatPerson2PersonJob {

    private final JobService jobService;

    @Scheduled(cron = "${statPerson2PersonJob.cron:0 10 2 * * ?}") // Default: run at 2:10 AM daily
    @Async("threadPoolExecutorForJob")
    public void generatePerson2PersonStatistics() {
        ThreadContext.put("uuid", UUID.randomUUID().toString().toUpperCase().replace("-", ""));

        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formattedDate = oneDayAgo.format(formatter);

        try {
            log.info("Starting person2person statistics job for date: {}", formattedDate);
            
            
            jobService.generatePerson2PersonStatistics(formattedDate, formattedDate, "JOB");
            log.info("Person2person statistics job completed successfully for date: {}", formattedDate);
        } catch (Exception e) {
            log.error("Error in person2person statistics job for date: {}", formattedDate, e);
        } finally {
            ThreadContext.clearAll();
        }
    }
}
