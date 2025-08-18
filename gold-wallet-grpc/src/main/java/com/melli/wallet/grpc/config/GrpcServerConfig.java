package com.melli.wallet.grpc.config;

import com.melli.wallet.grpc.interceptor.CompositeInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class GrpcServerConfig {

    private final CompositeInterceptor compositeInterceptor;

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @Bean
    public Server grpcServer() throws IOException {
        log.info("Starting gRPC server on port: {}", grpcPort);
        
        Server server = ServerBuilder.forPort(grpcPort)
                .intercept(compositeInterceptor)
                .build();
        
        server.start();
        log.info("gRPC server started successfully on port: {}", grpcPort);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server...");
            server.shutdown();
            log.info("gRPC server shut down successfully");
        }));
        
        return server;
    }
}
