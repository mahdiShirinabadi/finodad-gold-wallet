package com.melli.hub.web;

import jakarta.servlet.http.HttpServletRequest;

public class WebEndPoint {

    public String getIP(HttpServletRequest httpServletRequest) {
        String ip = httpServletRequest.getHeader("x-forwarded-for");
        if (ip == null) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip.split(",")[0];
    }

}
