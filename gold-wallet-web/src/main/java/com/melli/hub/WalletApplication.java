package com.melli.hub;


import com.melli.hub.config.FileStorageProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@OpenAPIDefinition(
        servers = {
                @Server(url = "/gold", description = "Default Server URL")
        }
)
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableCaching
@Log4j2
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class WalletApplication {

    public static void main(String[] args) {

        log.info("start app");

        SpringApplication.run(WalletApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
