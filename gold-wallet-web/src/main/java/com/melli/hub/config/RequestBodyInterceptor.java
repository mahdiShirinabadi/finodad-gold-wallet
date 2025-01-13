package com.melli.hub.config;

import com.melli.hub.service.LoggingHttpService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

@ControllerAdvice
public class RequestBodyInterceptor extends RequestBodyAdviceAdapter {

    final
    LoggingHttpService loggingService;

    final
    HttpServletRequest request;

    public RequestBodyInterceptor(LoggingHttpService loggingService, HttpServletRequest request) {
        this.loggingService = loggingService;
        this.request = request;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        loggingService.displayReq(request,body);
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }
}
