package com.melli.wallet.service.repository.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.StatusEntity;
import com.melli.wallet.domain.master.persistence.StatusRepository;
import com.melli.wallet.domain.slave.entity.ReportStatusEntity;
import com.melli.wallet.domain.slave.persistence.ReportStatusRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.mapper.StatusMapper;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.CustomStringUtils;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            throw new InternalServiceException("status not found", StatusRepositoryService.STATUS_NOT_FOUND, HttpStatus.OK);
        }
        return statusMapper.toStatusEntity(reportEntity);
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

        if (CustomStringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("code"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), 
                searchCriteria.get("code").toLowerCase()));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("name"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("persianDescription")), 
                "%" + searchCriteria.get("name").toLowerCase() + "%"));
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
    @CacheEvict(allEntries = true)
    public void createStatus(StatusEntity statusEntity) throws InternalServiceException {
        log.info("start createStatus with code: {}", statusEntity.getCode());
        
        try {
            statusRepository.save(statusEntity);
            log.info("Status created successfully with code: {}", statusEntity.getCode());
        } catch (Exception e) {
            log.error("Error creating status with code: {}", statusEntity.getCode(), e);
            throw new InternalServiceException("Error creating status", StatusRepositoryService.INTERNAL_ERROR, HttpStatus.OK);
        }
    }

    @Override
    @CacheEvict(allEntries = true)
    public void updateStatus(StatusEntity statusEntity) throws InternalServiceException {
        log.info("start updateStatus with id: {}", statusEntity.getId());
        
        // Check if status exists
        StatusEntity existingStatus = statusRepository.findById(statusEntity.getId()).orElse(null);
        if (existingStatus == null) {
            log.error("Status not found with id: {}", statusEntity.getId());
            throw new InternalServiceException("Status not found", StatusRepositoryService.STATUS_NOT_FOUND, HttpStatus.OK);
        }
        
        // Check if another status with same code exists (excluding current one)
        StatusEntity statusWithSameCode = statusRepository.findByCode(statusEntity.getCode());
        if (statusWithSameCode != null && (statusWithSameCode.getId() != (statusEntity.getId()))) {
            log.error("Another status with code {} already exists", statusEntity.getCode());
            throw new InternalServiceException("Another status with this code already exists", StatusRepositoryService.STATUS_NOT_FOUND, HttpStatus.OK);
        }
        
        try {
            statusRepository.save(statusEntity);
            log.info("Status updated successfully with id: {}", statusEntity.getId());
        } catch (Exception e) {
            log.error("Error updating status with id: {}", statusEntity.getId(), e);
            throw new InternalServiceException("Error updating status", StatusRepositoryService.INTERNAL_ERROR, HttpStatus.OK);
        }
    }

    @Override
    public Page<StatusEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable) {
        log.info("start findAllWithSpecification with searchCriteria: {}, pageable: {}", searchCriteria, pageable);
        Specification<StatusEntity> specification = getPredicate(searchCriteria);
        return statusRepository.findAll(specification, pageable);
    }
}
