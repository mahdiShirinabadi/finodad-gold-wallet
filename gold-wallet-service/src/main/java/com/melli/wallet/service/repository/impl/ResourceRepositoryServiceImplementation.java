package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.master.persistence.ResourceRepository;
import com.melli.wallet.service.repository.ResourceRepositoryService;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ResourceRepositoryServiceImplementation implements ResourceRepositoryService {

    private final ResourceRepository resourceRepository;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final Helper helper;

    @Override
    public ResourceEntity getRequestType(String name) {
        return resourceRepository.findByName(name);
    }

}
