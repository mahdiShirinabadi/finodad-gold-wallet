package com.melli.wallet.grpc.config;

import com.melli.wallet.grpc.interceptor.CustomAuthorizationInterceptor;
import com.melli.wallet.grpc.interceptor.LoggingInterceptor;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Class Name: GrpcInterceptorConfig
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 */
@Configuration
@EnableMethodSecurity()
public class GrpcInterceptorConfig {

    @Bean
    public GlobalServerInterceptorConfigurer globalInterceptorConfigurer(
            LoggingInterceptor loggingInterceptor,
            CustomAuthorizationInterceptor customAuthorizationInterceptor) {
        return registry -> {
            registry.add(loggingInterceptor);
            registry.add(customAuthorizationInterceptor);
        };
    }
}
