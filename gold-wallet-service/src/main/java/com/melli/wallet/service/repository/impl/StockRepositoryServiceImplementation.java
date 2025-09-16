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
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
}
