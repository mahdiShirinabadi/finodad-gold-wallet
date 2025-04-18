package com.melli.wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author ShirinAbadi.Mahdi
 */

@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfiguration {

    @Bean(name = "threadPoolExecutorForSlack")
    public ThreadPoolTaskExecutor getAsyncExecutorSlack() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("CustomExecutorSlack::");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
