package com.melli.wallet.service.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.LimitationGeneralEntity;
import com.melli.wallet.domain.master.persistence.LimitationGeneralRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.LimitationGeneralService;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class Name: LimitationGeneralServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 4/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = ConstantRedisName.WALLET_GENERAL_LIMITATION)
public class LimitationGeneralServiceImplementation implements LimitationGeneralService {

    private final LimitationGeneralRepository limitationGeneralRepository;
    private final RedisLockService redisLockService;

    @Override
    public List<LimitationGeneralEntity> getLimitationGeneralEntities() throws InternalServiceException {
        return limitationGeneralRepository.findAll();
    }

    @Override
    @Cacheable(key = "{#name}", unless = "#result == null")
    public LimitationGeneralEntity getSetting(String name) {
        log.info("general limitation with name ({}) read from database", name);
        return limitationGeneralRepository.findByNameAndEndTimeIsNull(name);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void clearCache() {
        log.info("Start clearing limitation ...");
    }

    @Override
    public void save(LimitationGeneralEntity setting) throws InternalServiceException{
        String key = setting.getName();
        redisLockService.runAfterLock(key, this.getClass(),()->{
            LimitationGeneralEntity limitationGeneralEntity = limitationGeneralRepository.findByName(setting.getName());
            if(limitationGeneralEntity != null) {
                log.error("limitationGeneralEntity with name ({}) already exists and set endTime", setting.getName());
                limitationGeneralEntity.setEndTime(setting.getEndTime());
                limitationGeneralRepository.save(limitationGeneralEntity);
            }
            limitationGeneralRepository.save(setting);
            return null;
        },key);
    }
}
