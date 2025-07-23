package com.melli.wallet.service.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.MerchantRepository;
import com.melli.wallet.domain.master.persistence.MerchantWalletAccountCurrencyRepository;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.MerchantService;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.WalletAccountCurrencyService;
import com.melli.wallet.service.WalletAccountService;
import com.melli.wallet.service.TransactionService;
import com.melli.wallet.service.RrnService;
import com.melli.wallet.service.RequestTypeService;
import com.melli.wallet.utils.RedisLockService;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;

/**
 * Class Name: MerchantServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = ConstantRedisName.WALLET_MERCHANT)
public class MerchantServiceImplementation implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantWalletAccountCurrencyRepository merchantWalletAccountCurrencyRepository;
    private final WalletAccountCurrencyService walletAccountCurrencyService;
    private final Helper helper;
    private final WalletAccountService walletAccountService;
    private final TransactionService transactionService;
    private final RrnService rrnService;
    private final RequestTypeService requestTypeService;
    private final RedisLockService redisLockService;

    @Override
    public MerchantWalletAccountCurrencyEntity checkPermissionOnCurrency(WalletAccountCurrencyEntity walletAccountCurrencyEntity, MerchantEntity merchant) throws InternalServiceException {

        List<MerchantWalletAccountCurrencyEntity> merchantWalletAccountCurrencyEntityList = merchantWalletAccountCurrencyRepository.findByWalletAccountCurrencyEntityAndMerchantEntity(walletAccountCurrencyEntity, merchant);
        if(merchantWalletAccountCurrencyEntityList.isEmpty()){
            log.info("merchant ({}) on currency ({}) dont have a permission", merchant.getName(), walletAccountCurrencyEntity.getName());
            throw new InternalServiceException("merchant not found on currency", StatusService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }
        return merchantWalletAccountCurrencyEntityList.getFirst();
    }

    @Override
    public MerchantResponse getMerchant(ChannelEntity channelEntity, String currency) throws InternalServiceException {
       WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccountCurrencyService.findCurrency(currency);
        List<MerchantWalletAccountCurrencyEntity> merchantWalletAccountCurrencyEntityList = merchantWalletAccountCurrencyRepository.findByWalletAccountCurrencyEntity(walletAccountCurrencyEntity);
        if(merchantWalletAccountCurrencyEntityList.isEmpty()){
            return new MerchantResponse();
        }
        return helper.fillMerchantResponse(merchantWalletAccountCurrencyEntityList.stream().map(MerchantWalletAccountCurrencyEntity::getMerchantEntity).toList());
    }

    @Override
    public WalletBalanceResponse getBalance(ChannelEntity channelEntity, String merchantId) throws InternalServiceException {
        log.info("start get balance for merchantId ({})", merchantId);
        MerchantEntity merchantEntity = merchantRepository.findById(Integer.parseInt(merchantId));
        if(merchantEntity == null){
            log.error("merchant {} not found", merchantId);
            throw new InternalServiceException("merchant not found", StatusService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(merchantEntity.getWalletEntity());
        return helper.fillWalletBalanceResponse(walletAccountEntityList, walletAccountService);
    }

    @Override
    @Cacheable(unless = "#result == null")
    public MerchantEntity  findById(int id) {
        return merchantRepository.findById(id);
    }

    @Override
    public void save(MerchantEntity merchant) {
        merchantRepository.save(merchant);
    }

    @Override
    @CacheEvict
    public void clearAllCache() {
        log.info("start clear all merchant");
    }

    @Override
    public String increaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException {

        // Validate merchant exists
        MerchantEntity merchant = findById(Integer.parseInt(merchantId));
        if (merchant == null) {
            log.error("Merchant with ID {} not found", merchantId);
            throw new InternalServiceException("Merchant not found", StatusService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
        }

        // Find wallet account
        WalletAccountEntity walletAccount = walletAccountService.findByAccountNumber(walletAccountNumber);
        if (walletAccount == null) {
            log.error("Wallet account with number {} not found", walletAccountNumber);
            throw new InternalServiceException("Wallet account not found", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        // Check if wallet account belongs to the merchant
        if (walletAccount.getWalletEntity().getId() != merchant.getWalletEntity().getId()) {
            log.error("Wallet account {} does not belong to merchant {}", walletAccountNumber, merchantId);
            throw new InternalServiceException("Wallet account does not belong to merchant", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        return redisLockService.runAfterLock(merchantId, this.getClass(), () -> {
            log.info("start increaseBalance for merchantId ({}) with amount ({})", merchantId, amount);

            
            // Generate RRN for transaction
            RrnEntity rrnEntity = rrnService.generateTraceId(
                    merchant.getWalletEntity().getNationalCode(),
                    channelEntity,
                    requestTypeService.getRequestType(RequestTypeService.MERCHANT_INCREASE_BALANCE),
                    walletAccountNumber,
                    amount
            );
            
            // Create transaction entity
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(new BigDecimal(amount));
            transaction.setWalletAccountEntity(walletAccount);
            transaction.setRrnEntity(rrnEntity);
            transaction.setRequestTypeId(requestTypeService.getRequestType(RequestTypeService.MERCHANT_INCREASE_BALANCE).getId());
            transaction.setAdditionalData("Manual increase by admin");
            
            // Create description
            transaction.setDescription("افزایش مانده پذیرنده " + merchant.getName() + " به مبلغ " + amount + " - شناسه تراکنش: " + rrnEntity.getId());
            
            // Execute deposit transaction
            transactionService.insertDeposit(transaction);
            
            log.info("finish increaseBalance for merchant {} with amount {} and traceId {}", merchantId, amount, rrnEntity.getUuid());
            return rrnEntity.getUuid();
        }, merchantId);
    }

    @Override
    public String decreaseBalance(ChannelEntity channelEntity, String walletAccountNumber, String amount, String merchantId) throws InternalServiceException {
        return redisLockService.runAfterLock(merchantId, this.getClass(), () -> {
            log.info("start decreaseBalance for merchantId ({}) with amount ({})", merchantId, amount);
            
            // Validate merchant exists
            MerchantEntity merchant = findById(Integer.parseInt(merchantId));
            if (merchant == null) {
                log.error("Merchant with ID {} not found", merchantId);
                throw new InternalServiceException("Merchant not found", StatusService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
            }
            
            // Find wallet account
            WalletAccountEntity walletAccount = walletAccountService.findByAccountNumber(walletAccountNumber);
            if (walletAccount == null) {
                log.error("Wallet account with number {} not found", walletAccountNumber);
                throw new InternalServiceException("Wallet account not found", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
            }
            
            // Check if wallet account belongs to the merchant
            if (walletAccount.getWalletEntity().getId() != merchant.getWalletEntity().getId()) {
                log.error("Wallet account {} does not belong to merchant {}", walletAccountNumber, merchantId);
                throw new InternalServiceException("Wallet account does not belong to merchant", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
            }
            
            // Check if merchant has sufficient balance
            BigDecimal currentBalance = walletAccountService.getBalance(walletAccount.getId());
            BigDecimal requestedAmount = new BigDecimal(amount);
            if (currentBalance.compareTo(requestedAmount) < 0) {
                log.error("Insufficient balance for merchant {}. Current: {}, Requested: {}", merchantId, currentBalance, requestedAmount);
                throw new InternalServiceException("Insufficient balance", StatusService.BALANCE_IS_NOT_ENOUGH, HttpStatus.OK);
            }
            
            // Generate RRN for transaction
            RrnEntity rrnEntity = rrnService.generateTraceId(
                    merchant.getWalletEntity().getNationalCode(),
                    channelEntity,
                    requestTypeService.getRequestType(RequestTypeService.MERCHANT_DECREASE_BALANCE),
                    walletAccountNumber,
                    amount
            );
            
            // Create transaction entity
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(requestedAmount);
            transaction.setWalletAccountEntity(walletAccount);
            transaction.setRrnEntity(rrnEntity);
            transaction.setRequestTypeId(requestTypeService.getRequestType(RequestTypeService.MERCHANT_DECREASE_BALANCE).getId());
            transaction.setAdditionalData("Manual decrease by admin");
            
            // Create description
            transaction.setDescription("کاهش مانده پذیرنده " + merchant.getName() + " به مبلغ " + amount + " - شناسه تراکنش: " + rrnEntity.getId());
            
            // Execute withdrawal transaction
            transactionService.insertWithdraw(transaction);
            
            log.info("finish decreaseBalance for merchant {} with amount {} and traceId {}", merchantId, amount, rrnEntity.getUuid());
            return rrnEntity.getUuid();
        }, merchantId);
    }
}
