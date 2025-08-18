package com.melli.wallet.grpc.interceptor;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.service.repository.ChannelRepositoryService;
import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@Log4j2
public class JwtAuthenticationInterceptor implements ServerInterceptor {

    private final ChannelRepositoryService channelRepositoryService;
    private final String jwtSecret;

    public JwtAuthenticationInterceptor(ChannelRepositoryService channelRepositoryService, 
                                      @Value("${jwt.secret}") String jwtSecret) {
        this.channelRepositoryService = channelRepositoryService;
        this.jwtSecret = jwtSecret;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String token = extractToken(headers);
        
        if (token == null) {
            log.warn("No JWT token found in request");
            call.close(Status.UNAUTHENTICATED.withDescription("No JWT token provided"), headers);
            return new ServerCall.Listener<>() {};
        }

        try {
            ChannelEntity channelEntity = validateToken(token);
            if (channelEntity == null) {
                log.warn("Invalid JWT token");
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid JWT token"), headers);
                return new ServerCall.Listener<>() {};
            }

            // Set channel entity in request context
            RequestContext.setChannelEntity(channelEntity);
            RequestContext.setUsername(channelEntity.getUsername());
            
            log.info("Authentication successful for channel: {}", channelEntity.getUsername());
            
            return next.startCall(call, headers);
            
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            call.close(Status.UNAUTHENTICATED.withDescription("Authentication failed"), headers);
            return new ServerCall.Listener<>() {};
        }
    }

    private String extractToken(Metadata headers) {
        String authHeader = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }

    private ChannelEntity validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            if (username == null) {
                log.warn("No username found in JWT token");
                return null;
            }

            // Check if token is expired
            if (claims.getExpiration() != null && claims.getExpiration().before(new java.util.Date())) {
                log.warn("JWT token is expired for user: {}", username);
                return null;
            }

            // Get channel entity from database
            ChannelEntity channelEntity = channelRepositoryService.findByUsername(username);
            if (channelEntity == null) {
                log.warn("Channel not found for username: {}", username);
                return null;
            }

            // Check if channel is active
            if (!"ACTIVE".equals(channelEntity.getStatus())) {
                log.warn("Channel is not active for username: {}", username);
                return null;
            }

            return channelEntity;
            
        } catch (Exception e) {
            log.error("Error validating JWT token: {}", e.getMessage(), e);
            return null;
        }
    }
}
