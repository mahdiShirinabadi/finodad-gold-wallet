package com.melli.wallet.grpc.config;

import com.melli.wallet.grpc.interceptor.AuthInterceptor;
import com.melli.wallet.grpc.interceptor.CustomAuthorizationInterceptor;
import com.melli.wallet.grpc.interceptor.LoggingInterceptor;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Class Name: GrpcInterceptorConfig
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 */
@Configuration
public class GrpcInterceptorConfig {

    @Bean
    public GlobalServerInterceptorConfigurer globalInterceptorConfigurer(
            LoggingInterceptor loggingInterceptor,
            AuthInterceptor authInterceptor,
            CustomAuthorizationInterceptor customAuthorizationInterceptor) {
        return registry -> {
            registry.add(loggingInterceptor);
            registry.add(authInterceptor);
            registry.add(customAuthorizationInterceptor);
        };
    }
}
