package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.TemplateEntity;
import com.melli.wallet.domain.master.persistence.TemplateRepository;
import com.melli.wallet.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.melli.wallet.ConstantRedisName.WALLET_TEMPLATE;

/**
 * Class Name: TemplateServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/26/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames=WALLET_TEMPLATE)
public class TemplateServiceImplementation implements TemplateService {

    private final TemplateRepository templateRepository;

    @Override
    @Cacheable(unless = "#result == null")
    public String getTemplate(String name) {
        log.info("start get template by name ==> ({})", name);
        return templateRepository.findByName(name) != null ? templateRepository.findByName(name).getValue():name;
    }

    @Override
    public List<TemplateEntity> findAllTemplate() {
        Iterable<TemplateEntity> templateIterable = templateRepository.findAll();
        return StreamSupport.stream(templateIterable.spliterator(), false).collect(Collectors.toList());
    }
}
