package com.melli.hub.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LoggingHttpService {

    void displayReq(HttpServletRequest request, Object body);

    void displayResp(HttpServletRequest request, HttpServletResponse response, Object body);
}
