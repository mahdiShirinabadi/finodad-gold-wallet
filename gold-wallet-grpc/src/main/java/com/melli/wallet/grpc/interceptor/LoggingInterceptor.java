package com.melli.wallet.grpc.interceptor;

import io.grpc.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Class Name: LoggingInterceptor
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 */
@Component
@Log4j2
public class LoggingInterceptor implements ServerInterceptor {

    @PostConstruct
    public void init() {
        log.info("LoggingInterceptor initialized");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String uuid = UUID.randomUUID().toString();
        ThreadContext.put("uuid", uuid);

        String clientIp = extractClientIp(call);
        if (clientIp == null) {
            clientIp = Objects.requireNonNull(call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)).toString();
        }
        ThreadContext.put("ip", clientIp);

        log.info("Received request: {} with UUID: {} from IP: {}", call.getMethodDescriptor().getFullMethodName(), uuid, clientIp);
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
            @Override
            public void onComplete() {
                try {
                    super.onComplete();
                } finally {
                    ThreadContext.remove("uuid");
                    ThreadContext.remove("ip");
                    ThreadContext.remove("username");
                }
            }

            @Override
            public void onCancel() {
                try {
                    super.onCancel();
                } finally {
                    ThreadContext.remove("uuid");
                    ThreadContext.remove("ip");
                }
            }
        };
    }

    private String extractClientIp(ServerCall<?, ?> call) {
        Attributes attributes = call.getAttributes();
        String clientIp = attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString();
        if (clientIp.startsWith("/")) {
            clientIp = clientIp.substring(1);
        }
        int colonIndex = clientIp.indexOf(':');
        if (colonIndex != -1) {
            clientIp = clientIp.substring(0, colonIndex);
        }
        return clientIp;
    }


}
