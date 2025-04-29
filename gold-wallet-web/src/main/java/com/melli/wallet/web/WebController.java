package com.melli.wallet.web;

import jakarta.servlet.http.HttpServletRequest;

public class WebController {

    public String getIP(HttpServletRequest httpServletRequest) {
        String ip = httpServletRequest.getHeader("x-forwarded-for");
        if (ip == null) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip.split(",")[0];
    }

}
