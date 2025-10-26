package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.StatPerson2PersonEntity;
import com.melli.wallet.domain.master.persistence.StatPerson2PersonRepository;
import com.melli.wallet.service.repository.StatPerson2PersonRepositoryService;
import com.melli.wallet.util.CustomStringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class Name: StatPerson2PersonRepositoryServiceImplementation
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Implementation of StatPerson2PersonRepositoryService with filtering capabilities
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class StatPerson2PersonRepositoryServiceImplementation implements StatPerson2PersonRepositoryService {

    private final StatPerson2PersonRepository statPerson2PersonRepository;

    @Override
    public Page<StatPerson2PersonEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable) {
        log.info("start findAllWithSpecification with searchCriteria: {}, pageable: {}", searchCriteria, pageable);
        Specification<StatPerson2PersonEntity> specification = getPredicate(searchCriteria);
        return statPerson2PersonRepository.findAll(specification, pageable);
    }

    public Specification<StatPerson2PersonEntity> getPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<StatPerson2PersonEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (CustomStringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("channelId"))) {
            predicates.add(criteriaBuilder.equal(root.get("channelId"), searchCriteria.get("channelId")));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("currencyId"))) {
            predicates.add(criteriaBuilder.equal(root.get("currencyId"), searchCriteria.get("currencyId")));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("result"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("result")), 
                "%" + searchCriteria.get("result").toLowerCase() + "%"));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("persianCalcDate"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("persianCalcDate")), 
                "%" + searchCriteria.get("persianCalcDate").toLowerCase() + "%"));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("fromDate"))) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("georgianCalcDate"), 
                java.sql.Date.valueOf(searchCriteria.get("fromDate"))));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("toDate"))) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("georgianCalcDate"), 
                java.sql.Date.valueOf(searchCriteria.get("toDate"))));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("minAmount"))) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), 
                new java.math.BigDecimal(searchCriteria.get("minAmount"))));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("maxAmount"))) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), 
                new java.math.BigDecimal(searchCriteria.get("maxAmount"))));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("minCount"))) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("count"), 
                Long.parseLong(searchCriteria.get("minCount"))));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("maxCount"))) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("count"), 
                Long.parseLong(searchCriteria.get("maxCount"))));
        }
        
        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<StatPerson2PersonEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }
}
