package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.TransactionEntity;
import com.melli.hub.domain.master.persistence.TransactionRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.StatusService;
import com.melli.hub.service.TransactionService;
import com.melli.hub.service.WalletAccountService;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Class Name: TransactionServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/26/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class TransactionServiceImplementation implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final RedisLockService redisLockService;
    private final WalletAccountService walletAccountService;

    @Override
    public List<TransactionEntity> walletLastTransaction(long walletAccountId, int limit) {
        return transactionRepository.findByWalletAccountEntityIdOrderByIdDesc(walletAccountId, PageRequest.of(0, limit));
    }

    @Override
    public void insertDeposit(TransactionEntity transaction) throws InternalServiceException {
        String key = String.valueOf(transaction.getWalletAccountEntity().getAccountNumber());

        redisLockService.runAfterLock(key, this.getClass(), () -> {
            log.info("start deposit amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
            long walletBalance = walletAccountService.getBalance(transaction.getWalletAccountEntity().getId());
            transaction.setType(TransactionEntity.DEPOSIT);
            transaction.setCreatedAt(new Date());
            transaction.setCreatedBy("System");
            transaction.setBalance(walletBalance + transaction.getAmount());
            walletAccountService.increaseBalance(transaction.getWalletAccountEntity().getId(), transaction.getAmount());
            transactionRepository.save(transaction);
            log.info("finish deposit amount ( {} ) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
            return null;
        }, transaction.getAdditionalData());
    }

    @Override
    public void insertWithdraw(TransactionEntity transaction) throws InternalServiceException {
        String key = String.valueOf(transaction.getWalletAccountEntity().getId());

        redisLockService.runAfterLock(key, this.getClass(), () -> {
            log.info("start withdraw amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
            long walletBalance = walletAccountService.getBalance(transaction.getWalletAccountEntity().getId());
            if (walletBalance - transaction.getAmount() < 0) {
                log.error("Balance of wallet({}) now is ({}), is less than withdraw amount({}) !!!", transaction.getWalletAccountEntity().getId(), walletBalance, transaction.getAmount());
                throw new InternalServiceException("Balance of walletAccountId( " + transaction.getWalletAccountEntity().getId() + "), is less than withdraw amount(" + transaction.getAmount() + ") !!!", StatusService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }

            transaction.setBalance(walletBalance - transaction.getAmount());
            transaction.setType(TransactionEntity.WITHDRAW);
            transaction.setCreatedAt(new Date());
            transaction.setCreatedBy("System");
            int result = walletAccountService.decreaseBalance(transaction.getWalletAccountEntity().getId(), transaction.getAmount());
            if (result <= 0) {
                log.error("Balance of wallet({}) now is ({}), is less than withdraw amount({}) !!!", transaction.getWalletAccountEntity().getId(), walletBalance, transaction.getAmount());
                throw new InternalServiceException("Balance of walletAccountId( " + transaction.getWalletAccountEntity().getId() + "), is less than withdraw amount(" + transaction.getAmount() + ") !!!", StatusService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }
            transactionRepository.save(transaction);
            log.info("finish withdraw amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
            return null;
        }, transaction.getAdditionalData());
    }
}
