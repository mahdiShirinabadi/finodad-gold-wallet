package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.TemplateEntity;
import com.melli.wallet.domain.master.persistence.TemplateRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.TemplateRepositoryService;
import com.melli.wallet.util.CustomStringUtils;
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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
public class TemplateRepositoryServiceImplementation implements TemplateRepositoryService {

    private final TemplateRepository templateRepository;

    @Override
    @Cacheable(unless = "#result == null")
    public String getTemplate(String name) {
        log.info("start get template by name ==> ({})", name);
        return templateRepository.findByName(name) != null ? templateRepository.findByName(name).getValue():name;
    }


    @Override
    public TemplateEntity findById(Long id) throws InternalServiceException {
        log.info("start findById with id: {}", id);
        TemplateEntity templateEntity = templateRepository.findById(id).orElse(null);
        if (templateEntity == null) {
            log.error("Template not found with id: {}", id);
            throw new InternalServiceException("Template not found", StatusRepositoryService.TEMPLATE_NOT_FOUND, HttpStatus.OK);
        }
        return templateEntity;
    }

    @Override
    public TemplateEntity findByName(String name){
        log.info("start findByName with id: {}", name);
        return templateRepository.findByName(name);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void createTemplate(TemplateEntity templateEntity) throws InternalServiceException {
        log.info("start createTemplate with name: {}", templateEntity.getName());
        try {
            templateRepository.save(templateEntity);
            log.info("Template created successfully with name: {}", templateEntity.getName());
        } catch (Exception e) {
            log.error("Error creating template with name: {}", templateEntity.getName(), e);
            throw new InternalServiceException("Error creating template", StatusRepositoryService.INTERNAL_ERROR, HttpStatus.OK);
        }
    }

    @Override
    @CacheEvict(allEntries = true)
    public void updateTemplate(TemplateEntity templateEntity) throws InternalServiceException {
        log.info("start updateTemplate with id: {}", templateEntity.getId());
        try {
            templateRepository.save(templateEntity);
            log.info("Template updated successfully with id: {}", templateEntity.getId());
        } catch (Exception e) {
            log.error("Error updating template with id: {}", templateEntity.getId(), e);
            throw new InternalServiceException("Error updating template", StatusRepositoryService.INTERNAL_ERROR, HttpStatus.OK);
        }
    }

    @Override
    public Page<TemplateEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable) {
        log.info("start findAllWithSpecification with searchCriteria: {}, pageable: {}", searchCriteria, pageable);
        Specification<TemplateEntity> specification = getPredicate(searchCriteria);
        return templateRepository.findAll(specification, pageable);
    }

    public Specification<TemplateEntity> getPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<TemplateEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (CustomStringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("code"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), 
                "%" + searchCriteria.get("code").toLowerCase() + "%"));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("name"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), 
                "%" + searchCriteria.get("name").toLowerCase() + "%"));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("value"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("value")), 
                "%" + searchCriteria.get("value").toLowerCase() + "%"));
        }
        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<TemplateEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }
}
