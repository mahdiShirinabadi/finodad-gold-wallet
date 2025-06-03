package com.melli.wallet.grpc.security;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.service.ChannelAccessTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component

@Log4j2
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtChannelDetailsService jwtChannelDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final RequestContext requestContext;
    private final ChannelAccessTokenService channelAccessTokenService;

    public JwtRequestFilter(JwtChannelDetailsService jwtChannelDetailsService, JwtTokenUtil jwtTokenUtil, RequestContext requestContext, ChannelAccessTokenService channelAccessTokenService) {
        this.jwtChannelDetailsService = jwtChannelDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.requestContext = requestContext;
        this.channelAccessTokenService = channelAccessTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token

        resolveClientIP(request);
        ThreadContext.put("uuid", UUID.randomUUID().toString().toUpperCase().replace("-", ""));
        ThreadContext.put("ipAddress", requestContext.getClientIp());

        log.info("Request URI: {}", request.getRequestURI());
        if (requestTokenHeader != null && requestTokenHeader.toLowerCase().startsWith("bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                ThreadContext.put("username", username);
            } catch (IllegalArgumentException e) {
                log.error("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                log.error("JWT Token has expired");
            }
        } else if (requestTokenHeader != null) {
            log.error("JWT Token does not begin with Bearer String");
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            ChannelEntity channelEntity = (ChannelEntity) this.jwtChannelDetailsService.loadUserByUsername(username);

            // if token is valid configure Spring Security to manually set authentication
            if (channelEntity != null) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        channelEntity, null, channelEntity.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
            requestContext.setChannelEntity(channelEntity);
        }
        chain.doFilter(request, response);
        ThreadContext.clearAll();
    }

    private void resolveClientIP(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (!StringUtils.hasText(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        requestContext.setClientIp(remoteAddr);
    }
}
