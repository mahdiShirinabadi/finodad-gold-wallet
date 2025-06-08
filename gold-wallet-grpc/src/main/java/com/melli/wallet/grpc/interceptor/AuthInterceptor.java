package com.melli.wallet.grpc.interceptor;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.grpc.security.JwtChannelDetailsService;
import com.melli.wallet.grpc.security.JwtTokenUtil;
import com.melli.wallet.service.ChannelService;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;

/**
 * Class Name: AuthInterceptor
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 */
@Component
@RequiredArgsConstructor
@Log4j2
@GrpcGlobalServerInterceptor
public class AuthInterceptor implements ServerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtChannelDetailsService jwtChannelDetailsService;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        log.info("AuthInterceptor interceptCall called for method: {}", methodName);

        // Set up thread-local context
        String clientIp = extractClientIp(call);
        String uuid = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        RequestContext.setClientIp(clientIp);
        RequestContext.setUuid(uuid);

        if (methodName.endsWith("AuthService/Login") || methodName.endsWith("AuthService/RefreshToken")) {
            return next.startCall(call, headers);
        }

        String authHeader = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid Authorization header"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);

            ChannelEntity channelEntity = (ChannelEntity) jwtChannelDetailsService.loadUserByUsername(username);
            if (channelEntity == null) {
                call.close(Status.UNAUTHENTICATED.withDescription("user not found"), new Metadata());
                return new ServerCall.Listener<>() {};
            }

            if (!jwtTokenUtil.validateToken(token, username)) {
                log.error("Invalid token for method: {}", methodName);
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), new Metadata());
                return new ServerCall.Listener<>() {};
            }

            // Set SecurityContext
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, channelEntity.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            ThreadContext.put("username", username);
            RequestContext.setUsername(username);
            RequestContext.setChannelEntity(channelEntity);
            return next.startCall(call, headers);
        } catch (Exception e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Token validation failed: " + e.getMessage()), new Metadata());
            log.error("Token validation failed for method: {}: {}", methodName, e.getMessage());
            return new ServerCall.Listener<>() {};
        }
    }

    private String extractClientIp(ServerCall<?, ?> call) {
        SocketAddress remoteAddr = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        if (remoteAddr == null) return "unknown";

        String clientIp = remoteAddr.toString();
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
