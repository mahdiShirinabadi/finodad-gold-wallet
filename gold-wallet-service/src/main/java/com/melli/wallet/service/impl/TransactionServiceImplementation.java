package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.TransactionEntity;
import com.melli.wallet.domain.master.persistence.TransactionRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.TransactionService;
import com.melli.wallet.service.WalletAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final WalletAccountService walletAccountService;

    @Override
    public List<TransactionEntity> walletLastTransaction(long walletAccountId, int limit) {
        return transactionRepository.findByWalletAccountEntityIdOrderByIdDesc(walletAccountId, PageRequest.of(0, limit));
    }

    /**
     * If called outside a transaction, it throws an exception, preventing non-transactional execution
     * @param transaction
     * @throws InternalServiceException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertDeposit(TransactionEntity transaction) throws InternalServiceException {
        log.info("start deposit amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
        BigDecimal walletBalance = walletAccountService.getBalance(transaction.getWalletAccountEntity().getId());
        transaction.setType(TransactionEntity.DEPOSIT);
        transaction.setCreatedAt(new Date());
        transaction.setCreatedBy("System");
        transaction.setBalance(walletBalance.add(transaction.getAmount()));
        walletAccountService.increaseBalance(transaction.getWalletAccountEntity().getId(), transaction.getAmount());
        transactionRepository.save(transaction);
        log.info("finish deposit amount ( {} ) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertWithdraw(TransactionEntity transaction) throws InternalServiceException {

        log.info("start withdraw amount ({}) from walletAccountId ({})", transaction.getAmount(), transaction.getWalletAccountEntity().getId());
        // Get current balance
        BigDecimal walletBalance = walletAccountService.getBalance(transaction.getWalletAccountEntity().getId());

        // Check for sufficient balance
        if (walletBalance.subtract(transaction.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            log.error("Balance of wallet({}) now is ({}), is less than withdraw amount({}) !!!", transaction.getWalletAccountEntity().getId(), walletBalance, transaction.getAmount());
            throw new InternalServiceException("Balance of walletAccountId( " + transaction.getWalletAccountEntity().getId() + "), is less than withdraw amount(" + transaction.getAmount() + ") !!!", StatusService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
        }

        transaction.setBalance(walletBalance.subtract(transaction.getAmount()));
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
    }
}
