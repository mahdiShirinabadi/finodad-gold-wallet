package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ProfileBlockEntity;
import com.melli.hub.domain.master.entity.ProfileEntity;
import com.melli.hub.domain.master.persistence.ProfileBlockRepository;
import com.melli.hub.service.ProfileBlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Class Name: ProfileBlockServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@RequiredArgsConstructor
@Log4j2
@Service
public class ProfileBlockServiceImplementation implements ProfileBlockService {

    private final ProfileBlockRepository profileBlockRepository;

    @Override
    public ProfileBlockEntity findByProfile(ProfileEntity profileEntity)
    {
        return profileBlockRepository.findByProfileEntityId(profileEntity.getId());
    }

    @Override
    public void save(ProfileBlockEntity profileBlockEntity) {
        profileBlockRepository.save(profileBlockEntity);
    }

    @Override
    public void clearCache() {

    }
}
