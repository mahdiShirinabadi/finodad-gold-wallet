package com.melli.hub.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.util.TimeZone;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "15m")
@Profile({"dev", "staging", "prod","test"})
public class ShedLockConfig {


   /* @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schemaName;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .withTimeZone(TimeZone.getTimeZone("GMT+3:30"))
                        .withTableName(schemaName + ".shedlock")
                        .build()
        );

    }*/
}
