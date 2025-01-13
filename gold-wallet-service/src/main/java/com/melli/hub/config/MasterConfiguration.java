package com.melli.hub.config;


import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        entityManagerFactoryRef = "masterDSEmFactory",
        transactionManagerRef = "masterDSTransactionManager",
        basePackages = "com.melli.hub.domain.master"
)
@Profile({"dev","prod","staging","test"})
public class MasterConfiguration {

    private final Environment env;

    public MasterConfiguration(Environment env) {
        this.env = env;
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties masterDSProperties(){
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource masterDS(@Qualifier("masterDSProperties") DataSourceProperties masterDSProperties){
        HikariDataSource hikariDataSource = masterDSProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        hikariDataSource.setPoolName(String.valueOf(env.getProperty("spring.datasource.hikari.pool-name")));
        hikariDataSource.setMaximumPoolSize(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.datasource.hikari.maximum-pool-size"))));
        hikariDataSource.setMaxLifetime(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.datasource.hikari.max-lifetime"))));
        hikariDataSource.setIdleTimeout(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.datasource.hikari.idle-timeout"))));
        hikariDataSource.setConnectionTimeout(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.datasource.hikari.connection-timeout"))));
        hikariDataSource.setMinimumIdle(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.datasource.hikari.minimum-idle"))));
        return hikariDataSource;
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean masterDSEmFactory(@Qualifier("masterDS") DataSource masterDS, EntityManagerFactoryBuilder builder){
        return builder.dataSource ( masterDS ).packages ("com.melli.hub.domain.master").build ();
    }

    @Primary
    @Bean
    public PlatformTransactionManager masterDSTransactionManager(@Qualifier("masterDSEmFactory") EntityManagerFactory masterDSEmFactory){
        return new JpaTransactionManager( masterDSEmFactory );
    }
}
