package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.SettingGeneralEntity;
import com.melli.hub.domain.master.persistence.SettingGeneralRepository;
import com.melli.hub.service.SettingGeneralService;
import com.melli.hub.utils.Helper;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import static com.melli.hub.utils.Constant.SETTING_NAME_CACHE;

@Service
@CacheConfig(cacheNames = SETTING_NAME_CACHE)
@Log4j2
@RequiredArgsConstructor
public class SettingGeneralServiceImplementation implements SettingGeneralService {

    private final SettingGeneralRepository settingGeneralRepository;
    private final Helper helper;
    private final RedisLockService redisLockService;
    private final RedisCacheManager cacheManager;

    @Override
    @Cacheable(key = "{#name}", unless = "#result == null")
    public SettingGeneralEntity getSetting(String name) {
        return settingGeneralRepository.findByNameAndEndTimeIsNull(name);
    }

    @CacheEvict(allEntries = true)
    @Override
    public void clearCache() {
        log.info("Start clearing setting ...");
    }

    @Override
    public void save(SettingGeneralEntity setting) {
        settingGeneralRepository.save(setting);
    }
}
