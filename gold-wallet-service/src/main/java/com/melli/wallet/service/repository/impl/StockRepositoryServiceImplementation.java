package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.StockHistoryRepository;
import com.melli.wallet.domain.master.persistence.StockRepository;
import com.melli.wallet.domain.response.stock.StockCurrencyListResponse;
import com.melli.wallet.domain.response.stock.StockListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.AlertService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.StockRepositoryService;
import com.melli.wallet.service.repository.WalletAccountCurrencyRepositoryService;
import com.melli.wallet.util.CustomStringUtils;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class Name: StockRepositoryServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 9/15/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class StockRepositoryServiceImplementation implements StockRepositoryService {

    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final AlertService alertService;
    private final RedisLockService redisLockService;
    private final Helper helper;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;

    @Override
    public BigDecimal getBalance(long id) {
        return stockRepository.getBalance(id);
    }

    @Override
    public StockListResponse getAllBalance(WalletAccountCurrencyEntity walletAccountCurrencyEntity) {
        List<StockRepository.AggregationStockDTO> aggregationStockDTOList = stockRepository.getAllBalance(walletAccountCurrencyEntity.getId());
        return helper.fillStockList(aggregationStockDTOList);
    }

    @Override
    public StockCurrencyListResponse getSumBalanceByCurrency() {
        List<StockRepository.AggregationStockByCurrencyDTO> aggregationStockByCurrencyDTO = stockRepository.getSumBalance();
        return helper.fillStockCurrencyList(aggregationStockByCurrencyDTO, walletAccountCurrencyRepositoryService);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertDeposit(TransactionEntity transaction) throws InternalServiceException {

        log.info("start deposit amount ({}) from walletId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getWalletEntity().getId());
        StockEntity stockEntity = findStockByWallet(transaction.getWalletAccountEntity());

        if (stockEntity == null) {
            return;
        }

        String key = String.valueOf(stockEntity.getId());

        redisLockService.runAfterLock(key, this.getClass(), () -> {
            StockHistoryEntity stockHistoryEntity = new StockHistoryEntity();
            stockHistoryEntity.setAmount(transaction.getAmount());
            stockHistoryEntity.setStockEntity(stockEntity);
            stockHistoryEntity.setTransactionEntity(transaction);
            stockHistoryEntity.setAmount(transaction.getAmount());
            stockHistoryEntity.setType(transaction.getType());
            stockHistoryEntity.setBalance(stockRepository.getBalance(stockEntity.getId()).add(transaction.getAmount()));
            stockHistoryEntity.setCreatedBy("system");
            stockHistoryEntity.setCreatedAt(new Date());
            stockRepository.increaseBalance(stockEntity.getId(), transaction.getAmount());
            stockHistoryRepository.save(stockHistoryEntity);
            log.info("finish deposit amount ( {} ) from walletAccountId ({}), stockId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId(), stockEntity.getId());
            return null;
        }, transaction.getRrnEntity().getNationalCode());
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertWithdraw(TransactionEntity transaction) throws InternalServiceException {

        WalletEntity walletEntity = transaction.getWalletAccountEntity().getWalletEntity();
        log.info("start withdraw amount ({}) from walletId ({})", transaction.getAmount(), walletEntity.getId());
        StockEntity stockEntity = findStockByWallet(transaction.getWalletAccountEntity());

        if (stockEntity == null) {
            return;
        }

        String key = String.valueOf(stockEntity.getId());

        redisLockService.runAfterLock(key, this.getClass(), () -> {
            StockHistoryEntity stockHistoryEntity = new StockHistoryEntity();
            stockHistoryEntity.setAmount(transaction.getAmount());
            stockHistoryEntity.setStockEntity(stockEntity);
            stockHistoryEntity.setTransactionEntity(transaction);
            stockHistoryEntity.setAmount(transaction.getAmount());
            stockHistoryEntity.setType(transaction.getType());
            stockHistoryEntity.setBalance(stockRepository.getBalance(stockEntity.getId()).subtract(transaction.getAmount()));
            stockHistoryEntity.setCreatedBy("system");
            stockHistoryEntity.setCreatedAt(new Date());
            stockRepository.decreaseBalance(stockEntity.getId(), transaction.getAmount());
            stockHistoryRepository.save(stockHistoryEntity);
            log.info("finish deposit amount ( {} ) from walletId ({}), stockId ({})", transaction.getAmount(), walletEntity.getId(), stockEntity.getCode());
            return null;
        }, transaction.getRrnEntity().getNationalCode());
    }


    private StockEntity findStockByWallet(WalletAccountEntity walletAccountEntity) throws InternalServiceException {
        String code = walletAccountEntity.getWalletEntity().getNationalCode().substring(walletAccountEntity.getWalletEntity().getNationalCode().length() - 2);
        StockEntity stockEntity = stockRepository.findByCodeAndWalletAccountCurrencyEntity(code, walletAccountEntity.getWalletAccountCurrencyEntity());
        if (stockEntity == null) {
            log.error("stockEntity not found for nationalCode {}", walletAccountEntity.getWalletEntity().getNationalCode());
            String alert = String.format("منبع برای کد ملی (%s) یافت نشد", walletAccountEntity.getWalletEntity().getNationalCode());
            alertService.send(alert, String.valueOf(StatusRepositoryService.STOCK_NOT_FOUND));
            return null;
        }
        return stockEntity;
    }

    @Override
    public Page<StockEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable) {
        log.info("start findAllWithSpecification with searchCriteria: {}, pageable: {}", searchCriteria, pageable);
        Specification<StockEntity> specification = getPredicate(searchCriteria);
        return stockRepository.findAll(specification, pageable);
    }

    public Specification<StockEntity> getPredicate(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);
            String orderBy = Optional.ofNullable(searchCriteria.get("orderBy")).orElse("id");
            String sortDirection = Optional.ofNullable(searchCriteria.get("sort")).orElse("asc");

            setOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesFromCriteria(Map<String, String> searchCriteria, Root<StockEntity> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (CustomStringUtils.hasText(searchCriteria.get("id"))) {
            predicates.add(criteriaBuilder.equal(root.get("id"), searchCriteria.get("id")));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("code"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), 
                "%" + searchCriteria.get("code").toLowerCase() + "%"));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("name"))) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), 
                "%" + searchCriteria.get("name").toLowerCase() + "%"));
        }
        if (CustomStringUtils.hasText(searchCriteria.get("walletAccountCurrencyId"))) {
            predicates.add(criteriaBuilder.equal(root.get("walletAccountCurrencyEntity").get("id"), searchCriteria.get("walletAccountCurrencyId")));
        }
        return predicates;
    }

    private void setOrder(CriteriaQuery<?> query, String orderBy, String sortDirection, CriteriaBuilder criteriaBuilder, Root<StockEntity> root) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
        }
    }
}
