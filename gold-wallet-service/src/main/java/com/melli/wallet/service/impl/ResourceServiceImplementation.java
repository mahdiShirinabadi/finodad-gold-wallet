package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.master.persistence.ResourceRepository;
import com.melli.wallet.service.ResourceService;
import com.melli.wallet.service.SettingGeneralService;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ResourceServiceImplementation implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final SettingGeneralService settingGeneralService;
    private final Helper helper;

    @Override
    public ResourceEntity getRequestType(String name) {
        return resourceRepository.findByName(name);
    }

}
