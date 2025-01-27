package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.StatusEntity;
import com.melli.hub.domain.master.persistence.StatusRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.SettingGeneralService;
import com.melli.hub.service.StatusService;
import com.melli.hub.util.StringUtils;
import com.melli.hub.utils.Constant;
import com.melli.hub.utils.RedisLockService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Log4j2
@CacheConfig(cacheNames = Constant.STATUS_NAME_CACHE)
@RequiredArgsConstructor
public class StatusServiceImplementation implements StatusService {

    private final StatusRepository statusRepository;
    private final SettingGeneralService settingGeneralService;
    private final RedisLockService redisLockService;


    @PostConstruct
    @Override
    public void init() {
        log.info("Start init status .....");
    }

    @Override
    @Cacheable(key = "{#code}", unless = "#result == null")
    public StatusEntity findByCode(String code) {
        log.info("Starting retrieval of Status by code: {}", code);
        StatusEntity statusEntity = statusRepository.findByCode(code);

        if (statusEntity == null) {
            log.info("Status code not defined: {}", code);
            return createUndefinedStatusEntity(code);
        }
        return statusEntity;
    }

    private StatusEntity createUndefinedStatusEntity(String code) {
        StatusEntity undefinedStatus = new StatusEntity();
        undefinedStatus.setCode(code);
        undefinedStatus.setPersianDescription("کد تعریف نشده است");
        return undefinedStatus;
    }


    @Override
    @CacheEvict(allEntries = true)
    public void clearCache() {
        log.info("start clear status ...");
    }



    public StatusEntity findById(long id) throws InternalServiceException {
        return statusRepository.findById(id).orElseThrow(() -> {
            log.error("status with id {} not found", id);
            return new InternalServiceException("status not found", StatusService.STATUS_NOT_FOUND, HttpStatus.FORBIDDEN);
        });
    }

    private StatusEntity findByCodeOrNull(String code) {
        return statusRepository.findByCode(code);
    }

    private StatusEntity buildStatusEntity(String createdByUsername, String code, String persianDescription, String additionalData) {
        StatusEntity entity = new StatusEntity();
        entity.setCreatedBy(createdByUsername);
        entity.setCreatedAt(new Date());
        entity.setCode(code);
        entity.setPersianDescription(persianDescription);
        entity.setAdditionalData(additionalData);
        return entity;
    }

    private StatusEntity save(StatusEntity statusEntity) {
        return statusRepository.save(statusEntity);
    }

    public Specification<StatusEntity> getPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<StatusEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }
        if (StringUtils.hasText(searchCriteria.get("code"))) {
            predicates.add(criteriaBuilder.equal(root.get("code"), searchCriteria.get("code")));
        }
        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<StatusEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }

    @Override
    public StatusEntity findByPersianDescription(String persianDescription) {
        return statusRepository.findByPersianDescription(persianDescription).orElse(null);
    }
}
