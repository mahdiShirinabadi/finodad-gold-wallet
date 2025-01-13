package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.SettingEntity;
import com.melli.hub.domain.master.persistence.SettingRepository;
import com.melli.hub.service.SettingService;
import com.melli.hub.utils.Helper;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.melli.hub.utils.Constant.SETTING_NAME_CACHE;

@Service
@CacheConfig(cacheNames = SETTING_NAME_CACHE)
@Log4j2
@RequiredArgsConstructor
public class SettingServiceImplementation implements SettingService {

    private final SettingRepository settingRepository;
    private final Helper helper;
    private final RedisLockService redisLockService;
    private final RedisCacheManager cacheManager;

    @Override
    @Cacheable(key = "{#name}", unless = "#result == null")
    public SettingEntity getSetting(String name) {
        return settingRepository.findByNameAndEndTimeIsNull(name);
    }

    @CacheEvict(allEntries = true)
    @Override
    public void clearCache() {
        log.info("Start clearing setting ...");
    }

    @Override
    public void save(SettingEntity setting) {
        settingRepository.save(setting);
    }
}
