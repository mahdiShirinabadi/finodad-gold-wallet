package com.melli.wallet.grpc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.melli.wallet.domain.master.entity.StatusEntity;
import com.melli.wallet.domain.response.base.ErrorDetail;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.utils.Helper;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    @Serial
    private static final long serialVersionUID = -7858869558953243875L;


    private final Helper helper;
    private final StatusService statusService;

    @Autowired
    public JwtAuthenticationEntryPoint(Helper helper, StatusService statusService) {
        this.helper = helper;
        this.statusService = statusService;
    }

    @Override
    @Timed(value = "service.commence")
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

        StatusEntity statusEntity = statusService.findByCode(String.valueOf(StatusService.TOKEN_NOT_VALID));
        ErrorDetail errorDetail = new ErrorDetail(statusEntity.getPersianDescription(), StatusService.TOKEN_NOT_VALID);

        response.getWriter().print(ow.writeValueAsString(helper.fillBaseResponse(false, errorDetail)));
    }
}