package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.operation.MerchantOperationService;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Class Name: MerchantOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 8/12/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class MerchantOperationServiceImplementation implements MerchantOperationService {

    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final TransactionRepositoryService transactionRepositoryService;
    private final RrnRepositoryService rrnRepositoryService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final RedisLockService redisLockService;
    private final MerchantRepositoryService merchantRepositoryService;
    private final Helper helper;

    @Override
    public WalletBalanceResponse getBalance(ChannelEntity channelEntity, String merchantId) throws InternalServiceException {
        log.info("start get balance for merchantId ({})", merchantId);
        MerchantEntity merchantEntity = merchantRepositoryService.findById(Integer.parseInt(merchantId));
        if(merchantEntity == null){
            log.error("merchant {} not found", merchantId);
            throw new InternalServiceException("merchant not found", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(merchantEntity.getWalletEntity());
        return helper.fillWalletBalanceResponse(walletAccountEntityList, walletAccountRepositoryService);
    }

    @Override
    public void updateStatus(ChannelEntity channelEntity, String merchantId, String status) throws InternalServiceException {
        log.info("start update status for merchantId ({}) and status ({}) with channel ({})", merchantId, status, channelEntity.getUsername());
        MerchantEntity merchantEntity = merchantRepositoryService.findById(Integer.parseInt(merchantId));
        if(merchantEntity == null){
            log.error("merchant {} not found", merchantId);
            throw new InternalServiceException("merchant not found", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }
        merchantEntity.setStatus(Integer.parseInt(status));
        merchantEntity.setUpdatedAt(new Date());
        merchantEntity.setUpdatedBy(channelEntity.getUsername());
        merchantRepositoryService.save(merchantEntity);
        merchantRepositoryService.clearAllCache();
        log.info("success update status for merchantId ({}) and status ({})", merchantId, status);
    }

    @Override
    public String increaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException {

        // Validate merchant exists
        MerchantEntity merchant = merchantRepositoryService.findById(Integer.parseInt(merchantId));
        if (merchant == null) {
            log.error("Merchant with ID {} not found", merchantId);
            throw new InternalServiceException("Merchant not found", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }

        // Find wallet account
        WalletAccountEntity walletAccount = walletAccountRepositoryService.findByAccountNumber(walletAccountNumber);
        if (walletAccount == null) {
            log.error("Wallet account with number {} not found", walletAccountNumber);
            throw new InternalServiceException("Wallet account not found", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        // Check if wallet account belongs to the merchant
        if (walletAccount.getWalletEntity().getId() != merchant.getWalletEntity().getId()) {
            log.error("Wallet account {} does not belong to merchant {}", walletAccountNumber, merchantId);
            throw new InternalServiceException("Wallet account does not belong to merchant", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        return redisLockService.runAfterLock(merchantId, this.getClass(), () -> {
            log.info("start increaseBalance for merchantId ({}) with amount ({})", merchantId, amount);


            // Generate RRN for transaction
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(
                    merchant.getWalletEntity().getNationalCode(),
                    channelEntity,
                    requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.MERCHANT_INCREASE_BALANCE),
                    walletAccountNumber,
                    amount
            );

            // Create transaction entity
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(new BigDecimal(amount));
            transaction.setWalletAccountEntity(walletAccount);
            transaction.setRrnEntity(rrnEntity);
            transaction.setRequestTypeId(requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.MERCHANT_INCREASE_BALANCE).getId());
            transaction.setAdditionalData("Manual increase by admin");

            // Create description
            transaction.setDescription("افزایش مانده پذیرنده " + merchant.getName() + " به مبلغ " + amount + " - شناسه تراکنش: " + rrnEntity.getId());

            // Execute deposit transaction
            transactionRepositoryService.insertDeposit(transaction);

            log.info("finish increaseBalance for merchant {} with amount {} and traceId {}", merchantId, amount, rrnEntity.getUuid());
            return rrnEntity.getUuid();
        }, merchantId);
    }

    @Override
    public String decreaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException {
        return redisLockService.runAfterLock(merchantId, this.getClass(), () -> {
            log.info("start decreaseBalance for merchantId ({}) with amount ({})", merchantId, amount);

            // Validate merchant exists
            MerchantEntity merchant = merchantRepositoryService.findById(Integer.parseInt(merchantId));
            if (merchant == null) {
                log.error("Merchant with ID {} not found", merchantId);
                throw new InternalServiceException("Merchant not found", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
            }

            // Find wallet account
            WalletAccountEntity walletAccount = walletAccountRepositoryService.findByAccountNumber(walletAccountNumber);
            if (walletAccount == null) {
                log.error("Wallet account with number {} not found", walletAccountNumber);
                throw new InternalServiceException("Wallet account not found", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
            }

            // Check if wallet account belongs to the merchant
            if (walletAccount.getWalletEntity().getId() != merchant.getWalletEntity().getId()) {
                log.error("Wallet account {} does not belong to merchant {}", walletAccountNumber, merchantId);
                throw new InternalServiceException("Wallet account does not belong to merchant", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
            }

            // Check if merchant has sufficient balance
            BigDecimal currentBalance = walletAccountRepositoryService.getBalance(walletAccount.getId());
            BigDecimal requestedAmount = new BigDecimal(amount);
            if (currentBalance.compareTo(requestedAmount) < 0) {
                log.error("Insufficient balance for merchant {}. Current: {}, Requested: {}", merchantId, currentBalance, requestedAmount);
                throw new InternalServiceException("Insufficient balance", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }

            // Generate RRN for transaction
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(
                    merchant.getWalletEntity().getNationalCode(),
                    channelEntity,
                    requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.MERCHANT_DECREASE_BALANCE),
                    walletAccountNumber,
                    amount
            );

            // Create transaction entity
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(requestedAmount);
            transaction.setWalletAccountEntity(walletAccount);
            transaction.setRrnEntity(rrnEntity);
            transaction.setRequestTypeId(requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.MERCHANT_DECREASE_BALANCE).getId());
            transaction.setAdditionalData("Manual decrease by admin");

            // Create description
            transaction.setDescription("کاهش مانده پذیرنده " + merchant.getName() + " به مبلغ " + amount + " - شناسه تراکنش: " + rrnEntity.getId());

            // Execute withdrawal transaction
            transactionRepositoryService.insertWithdraw(transaction);

            log.info("finish decreaseBalance for merchant {} with amount {} and traceId {}", merchantId, amount, rrnEntity.getUuid());
            return rrnEntity.getUuid();
        }, merchantId);
    }
}
