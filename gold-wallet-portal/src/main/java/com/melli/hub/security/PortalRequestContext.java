package com.melli.hub.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;


@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Setter
@Getter
public class PortalRequestContext {
    private String clientIp;
    private String accessToken;
    private PanelOperatorEntity panelOperatorEntity;
}
