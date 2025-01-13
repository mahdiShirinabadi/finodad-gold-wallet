package com.melli.hub.config;

import com.melli.hub.service.LoggingHttpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
@RequiredArgsConstructor
public class InterceptLog implements HandlerInterceptor {


    private final LoggingHttpService loggingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(request.getMethod().equals(HttpMethod.GET.name()) || request.getMethod().equals(HttpMethod.DELETE.name())|| request.getMethod().equals(HttpMethod.PUT.name()))    {
            loggingService.displayReq(request,null);
        }
        return true;
    }
}
