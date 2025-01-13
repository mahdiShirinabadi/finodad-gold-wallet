package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ResourceEntity;

public interface ResourceService {
    String LOGOUT = "LOGOUT";

    ResourceEntity getRequestType(String name);

}
