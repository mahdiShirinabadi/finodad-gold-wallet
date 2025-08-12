package com.melli.wallet.service.repository.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.StatusEntity;
import com.melli.wallet.domain.master.persistence.StatusRepository;
import com.melli.wallet.domain.slave.entity.ReportStatusEntity;
import com.melli.wallet.domain.slave.persistence.ReportStatusRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.mapper.StatusMapper;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.utils.RedisLockService;
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
@CacheConfig(cacheNames = ConstantRedisName.STATUS_NAME_CACHE)
@RequiredArgsConstructor
public class StatusRepositoryServiceImplementation implements StatusRepositoryService {

    private final StatusRepository statusRepository;
    private final ReportStatusRepository reportStatusRepository;
    private final StatusMapper statusMapper;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
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
        ReportStatusEntity reportEntity = reportStatusRepository.findByCode(code);

        if (reportEntity == null) {
            log.info("Status code not defined: {}", code);
            return createUndefinedStatusEntity(code);
        }
        return statusMapper.toStatusEntity(reportEntity);
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
        ReportStatusEntity reportEntity = reportStatusRepository.findById(id).orElse(null);
        if (reportEntity == null) {
            log.error("status with id {} not found", id);
            throw new InternalServiceException("status not found", StatusRepositoryService.STATUS_NOT_FOUND, HttpStatus.FORBIDDEN);
        }
        return statusMapper.toStatusEntity(reportEntity);
    }

    private StatusEntity findByCodeOrNull(String code) {
        ReportStatusEntity reportEntity = reportStatusRepository.findByCode(code);
        return reportEntity != null ? statusMapper.toStatusEntity(reportEntity) : null;
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
        ReportStatusEntity reportEntity = reportStatusRepository.findByPersianDescription(persianDescription).orElse(null);
        return reportEntity != null ? statusMapper.toStatusEntity(reportEntity) : null;
    }
}
