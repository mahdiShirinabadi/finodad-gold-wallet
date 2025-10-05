package com.melli.wallet;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Class Name: GrpcWalletApplication
 * Author: Mahdi Shirinabadi
 * Date: 6/2/2025
 */
@SpringBootApplication
@Log4j2
public class GrpcWalletApplication {
    public static void main(String[] args) {
        try {
            log.info("Starting GRPC Wallet Application...");
            ConfigurableApplicationContext context = SpringApplication.run(GrpcWalletApplication.class, args);
            log.info("GRPC Wallet Application started successfully!");
            
            // Log active profiles
            String[] activeProfiles = context.getEnvironment().getActiveProfiles();
            log.info("Active profiles: {}", String.join(", ", activeProfiles));
            
        } catch (Exception e) {
            log.error("Failed to start GRPC Wallet Application", e);
            System.exit(1);
        }
    }
}
