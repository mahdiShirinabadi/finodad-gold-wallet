package com.melli.wallet.config;


import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "slaveDSEmFactory",
        transactionManagerRef = "slaveDSTransactionManager",
        basePackages = "com.melli.wallet.domain.slave"
)
@Profile({"dev","prod","test","staging"})
public class SlaveConfiguration {

    private final Environment env;

    public SlaveConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    @ConfigurationProperties("spring.slave")
    public DataSourceProperties slaveDSProperties(){
        return new DataSourceProperties();
    }

    @Bean
    public DataSource slaveDS(@Qualifier("slaveDSProperties") DataSourceProperties slaveDSProperties){
        HikariDataSource hikariDataSource = slaveDSProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        hikariDataSource.setPoolName(String.valueOf(env.getProperty("spring.slave.hikari.pool-name")));
        hikariDataSource.setMaximumPoolSize(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.slave.hikari.maximum-pool-size"))));
        hikariDataSource.setMaxLifetime(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.slave.hikari.max-lifetime"))));
        hikariDataSource.setIdleTimeout(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.slave.hikari.idle-timeout"))));
        hikariDataSource.setConnectionTimeout(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.slave.hikari.connection-timeout"))));
        hikariDataSource.setMinimumIdle(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.slave.hikari.minimum-idle"))));
        return hikariDataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean slaveDSEmFactory(@Qualifier("slaveDS") DataSource slaveDS, EntityManagerFactoryBuilder builder){
        return builder.dataSource ( slaveDS ).packages ("com.melli.wallet.domain.slave").build ();
    }

    @Bean
    public PlatformTransactionManager slaveDSTransactionManager(@Qualifier("slaveDSEmFactory") EntityManagerFactory slaveDSEmFactory){
        return new JpaTransactionManager(slaveDSEmFactory);
    }
}
