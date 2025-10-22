package com.melli.wallet.service.repository.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.LimitationGeneralEntity;
import com.melli.wallet.domain.master.persistence.LimitationGeneralRepository;
import com.melli.wallet.domain.response.limitation.GeneralLimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.LimitationGeneralService;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.CustomStringUtils;
import com.melli.wallet.util.Utility;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private final Helper helper;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;

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
    public LimitationGeneralEntity getById(Long id) throws InternalServiceException {
        return limitationGeneralRepository.findById(id).orElseThrow(() -> {
            log.error("limitationGeneralEntity with id ({}) not found", id);
            return new InternalServiceException("General limitation with this ID not found", StatusRepositoryService.LIMITATION_NOT_FOUND, org.springframework.http.HttpStatus.OK);
        });
    }

    @Override
    @CacheEvict(allEntries = true)
    public void clearCache() {
        log.info("Start clearing limitation ...");
    }

    @Override
    public void save(LimitationGeneralEntity setting) throws InternalServiceException {
        String key = setting.getName();
        redisLockService.runAfterLock(key, this.getClass(), () -> {
            LimitationGeneralEntity limitationGeneralEntity = limitationGeneralRepository.findByName(setting.getName());
            limitationGeneralRepository.save(limitationGeneralEntity);
            return null;
        }, key);
    }

    @Override
    public GeneralLimitationListResponse getGeneralLimitationList(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException {
        log.info("start find all GeneralLimitation for username ({}), mapParameter ({})",
                channelEntity.getUsername(), Utility.mapToJsonOrNull(mapParameter));
        if (mapParameter == null) {
            mapParameter = new HashMap<>();
        }
        Pageable pageRequest = getPageableConfig(
                settingGeneralRepositoryService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));
        Page<LimitationGeneralEntity> limitationGeneralEntityPage = limitationGeneralRepository.findAll(getPredicate(mapParameter), pageRequest);
        return helper.fillGeneralLimitationListResponse(limitationGeneralEntityPage);
    }

    public Specification<LimitationGeneralEntity> getPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("desc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<LimitationGeneralEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (CustomStringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }

        if (CustomStringUtils.hasText(searchCriteria.get("name"))) {
            predicates.add(criteriaBuilder.equal(root.get("name"), searchCriteria.get("name")));
        }

        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<LimitationGeneralEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }
}
