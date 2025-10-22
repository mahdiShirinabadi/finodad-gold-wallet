package com.melli.wallet.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class FlywayConfig {

    @Bean
    @Profile({"prod","staging","dev"})
    public FlywayMigrationStrategy migrate() {
        return Flyway::migrate;
    }

    @Bean
    @Profile({"test","staging1","dev1"})
    public FlywayMigrationStrategy cleanMigrate() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
