package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.StockHistoryEntity;
import com.melli.wallet.domain.master.persistence.StockHistoryRepository;
import com.melli.wallet.service.repository.StockHistoryRepositoryService;
import com.melli.wallet.util.StringUtils;
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
 * Class Name: StockHistoryRepositoryServiceImplementation
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Implementation of stock history repository operations
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class StockHistoryRepositoryServiceImplementation implements StockHistoryRepositoryService {

    private final StockHistoryRepository stockHistoryRepository;

    @Override
    public Page<StockHistoryEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable) {
        log.info("start findAllWithSpecification with searchCriteria: {}, pageable: {}", searchCriteria, pageable);
        Specification<StockHistoryEntity> specification = getPredicate(searchCriteria);
        return stockHistoryRepository.findAll(specification, pageable);
    }

    public Specification<StockHistoryEntity> getPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<StockHistoryEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }
        if (StringUtils.hasText(searchCriteria.get("stockId"))) {
            predicates.add(criteriaBuilder.equal(root.get("stockEntity").get("id"), searchCriteria.get("stockId")));
        }
        if (StringUtils.hasText(searchCriteria.get("transactionId"))) {
            predicates.add(criteriaBuilder.equal(root.get("transactionEntity").get("id"), searchCriteria.get("transactionId")));
        }
        if (StringUtils.hasText(searchCriteria.get("type"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("type")), 
                "%" + searchCriteria.get("type").toLowerCase() + "%"));
        }
        if (StringUtils.hasText(searchCriteria.get("amount"))) {
            predicates.add(criteriaBuilder.equal(root.get("amount"), searchCriteria.get("amount")));
        }
        if (StringUtils.hasText(searchCriteria.get("balance"))) {
            predicates.add(criteriaBuilder.equal(root.get("balance"), searchCriteria.get("balance")));
        }
        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<StockHistoryEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }
}
