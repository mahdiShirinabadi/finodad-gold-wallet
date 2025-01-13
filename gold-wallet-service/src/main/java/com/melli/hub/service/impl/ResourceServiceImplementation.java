package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ResourceEntity;
import com.melli.hub.domain.master.persistence.ResourceRepository;
import com.melli.hub.service.ResourceService;
import com.melli.hub.service.SettingService;
import com.melli.hub.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ResourceServiceImplementation implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final SettingService settingService;
    private final Helper helper;

    @Override
    public ResourceEntity getRequestType(String name) {
        return resourceRepository.findByName(name);
    }

}
