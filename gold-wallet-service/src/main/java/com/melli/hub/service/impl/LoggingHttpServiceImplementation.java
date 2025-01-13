package com.melli.hub.service.impl;

import com.melli.hub.service.LoggingHttpService;
import com.melli.hub.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class LoggingHttpServiceImplementation implements LoggingHttpService {


    @Override
    public void displayReq(HttpServletRequest request, Object body) {

        StringBuilder reqMessage = new StringBuilder();
        Map<String, String> parameters = getParameters(request);

        reqMessage.append("REQUEST ");
        reqMessage.append("method = [").append(request.getMethod()).append("]");
        reqMessage.append(" path = [").append(request.getRequestURI()).append("] ");

        if (!parameters.isEmpty()) {
            reqMessage.append(" parameters = [").append(parameters).append("] ");
        }

        if (!Objects.isNull(body)) {
            String values = Utility.mapToJsonOrNull(body);
            if (values != null && (values.contains("password") || values.toLowerCase().contains("password"))) {
                values = "this message contain password !!! *******";
            }
            reqMessage.append(" body = [").append(values).append("]");
        }

        log.info("log Request: {}", reqMessage);
    }

    @Override
    public void displayResp(HttpServletRequest request, HttpServletResponse response, Object body) {
        StringBuilder respMessage = new StringBuilder();
        Map<String, String> headers = getHeaders(response);
        respMessage.append("RESPONSE ");
        respMessage.append(" method = [").append(request.getMethod()).append("]");
        if (!headers.isEmpty()) {
            respMessage.append(" ResponseHeaders = [").append(headers).append("]");
        }
        if (body != null && body.toString().contains("HELP lettuce_command_completion_seconds_max Latency between command send and command")) {
        } else {
            respMessage.append(" responseBody = [").append(Utility.mapToJsonOrNull(body)).append("]");
        }

        log.info("logResponse: {}", respMessage);
    }

    private Map<String, String> getHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        Collection<String> headerMap = response.getHeaderNames();
        for (String str : headerMap) {
            headers.put(str, response.getHeader(str));
        }
        return headers;
    }

    private Map<String, String> getParameters(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = params.nextElement();
            String paramValue = request.getParameter(paramName);
            parameters.put(paramName, paramValue);
        }
        return parameters;
    }
}
