package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.*;
import com.melli.hub.domain.response.purchase.PurchaseResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.*;
import com.melli.hub.utils.Helper;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Class Name: PurchaseServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class PurchaseServiceImplementation implements PurchaseService {

    private final RedisLockService redisLockService;
    private final RrnService rrnService;
    private final RequestService requestService;
    private final SecurityService securityService;
    private final MerchantService merchantService;
    private final Helper helper;
    private final WalletService walletService;
    private final WalletAccountService walletAccountService;
    private final WalletTypeService walletTypeService;
    private final WalletLimitationService walletLimitationService;
    private final EscrowWalletAccountService escrowWalletAccountService;


    @Override
    public PurchaseResponse purchase(ChannelEntity channel, String uniqueIdentifier, String amount, String price, String account, String signData, String dataForCheckInVerify, String additionalData, String merchantId, String nationalCode) throws InternalServiceException {
        return redisLockService.runAfterLock(account, this.getClass(), ()->{
            log.info("start purchase for uniqueIdentifier ({}), nationalCode ({})", uniqueIdentifier, nationalCode);
            WalletAccountEntity walletAccountEntity;
            PurchaseRequestEntity purchaseRequestEntity;
            RrnEntity rrnEntity;
            log.info("checking existence of uniqueIdentifier({}) ...", uniqueIdentifier);
            rrnEntity = rrnService.checkRrn(uniqueIdentifier, channel);
            log.info("checking existence of uniqueIdentifier({}), is finished.", uniqueIdentifier);

            log.info("Start checking uniqueness of traceId({}) ...", rrnEntity.getId());
            requestService.checkTraceIdIsUnique(rrnEntity.getId(), new PurchaseRequestEntity());
            log.info("purchase: Checking uniqueness of traceId({}), is finished.", rrnEntity.getId());

            securityService.checkSign(channel, signData, dataForCheckInVerify);

            MerchantEntity merchantEntity = merchantService.findById(Integer.parseInt(merchantId));

            if (merchantEntity == null) {
                log.error("merchantId({}) doesn't exist!!!", merchantId);
                throw new InternalServiceException("merchant doesn't exist!!! ", StatusService.MERCHANT_IS_NOT_EXIST, HttpStatus.OK);
            }
            Optional<WalletTypeEntity> walletTypeEntity = walletTypeService.getAll().stream().filter(x -> x.getName().equals(WalletTypeService.NORMAL_USER)).findFirst();
            walletAccountEntity = helper.checkWalletAndWalletAccount(walletService, rrnEntity.getNationalCode() , walletAccountService, account,walletTypeEntity.get());
            log.info("Start checking purchase's limitation for walletAccount({}) and channel({}) ...", walletAccountEntity.getId(), channel.getId());
            walletLimitationService.checkPurchaseLimitation(channel, walletAccountEntity.getWalletEntity(), Long.parseLong(amount), walletAccountEntity, merchantEntity);
            log.info("checking purchase's limitation for walletAccount ({}) and channel({}) is finished.", walletAccountEntity.getId(), channel.getId());
            purchaseRequestEntity = new PurchaseRequestEntity();
            purchaseRequestEntity.setPrice(Long.parseLong(price));
            purchaseRequestEntity.setAmount(new BigDecimal(amount));
            purchaseRequestEntity.setWalletAccount(walletAccountEntity);
            purchaseRequestEntity.setEscrowWalletAccount();
            purchaseRequestEntity.setRrnEntity();
            purchaseRequestEntity.setMerchantEntity();
            purchaseRequestEntity.setNationalCode();
            purchaseRequestEntity.setTerminalAmount();
            purchaseRequestEntity.setAdditionalData();
            purchaseRequestEntity.setRefNumber();
            purchaseRequestEntity.setCommissionAmount();
            purchaseRequestEntity.setCommissionMerchantAmount();
            purchaseRequestEntity.setCommissionChannelAmount();
            purchaseRequestEntity.setCommissionFinodadAmount();
            purchaseRequestEntity.setCommissionPercent();
            purchaseRequestEntity.setCommissionMerchantPercent();
            purchaseRequestEntity.setCommissionChannelPercent();
            purchaseRequestEntity.setCommissionFinodadPercent();
            purchaseRequestEntity.setTransactionType();
            purchaseRequestEntity.setTotalAmount();
            purchaseRequestEntity.setTerminalId();
            purchaseRequestEntity.setChannel();
            purchaseRequestEntity.setResult();
            purchaseRequestEntity.setChannelIp();
            purchaseRequestEntity.setCustomerIp();
            purchaseRequestEntity.setRequestTypeEntity();
            purchaseRequestEntity.setCreatedBy();
            purchaseRequestEntity.setUpdatedBy();
            purchaseRequestEntity.setCreatedAt();
            purchaseRequestEntity.setUpdatedAt();
            purchaseRequestEntity.setId();
            




        }, uniqueIdentifier);
    }

    @Override
    public PurchaseResponse verify(ChannelEntity channel, String traceId, String customerIp) throws InternalServiceException {
        return null;
    }

    @Override
    public PurchaseResponse reverse(ChannelEntity channel, String traceId, String channelIp) throws InternalServiceException {
        return null;
    }

    @Override
    public PurchaseTrackResponse purchaseTrack(ChannelEntity channel, String uid, String channelIp) throws InternalServiceException {
        return null;
    }

    @Override
    public void reverseWithoutUser(String traceNumber, String ip) throws InternalServiceException {

    }


    private WalletAccountEntity getEscrowWalletAccount(WalletAccountEntity userWalletAccountEntity, ChannelEntity channel) throws InternalServiceException {

        Optional<WalletTypeEntity> walletTypeEntity = walletTypeService.getAll().stream().filter(x -> x.getName().equals(WalletTypeService.CHANNEL)).findFirst();

        WalletEntity walletChannelEntity = walletService.findByNationalCodeAndWalletTypeId(channel.getNationalCode(), walletTypeEntity.get().getId());

        if (walletChannelEntity == null) {
            log.error("wallet for channel with mobile ({}) not found!!", channel.getMobile());
            throw new InternalServiceException("wallet for channel with mobile (" + channel.getNationalCode() + ") not found!!", StatusService.GENERAL_ERROR, HttpStatus.OK);
        }

        WalletAccountEntity walletAccount = escrowWalletAccountService.findEscrowWalletAccountBySourceWallet(userWalletAccountEntity, walletChannel);

        if (walletAccount == null) {
            log.error("walletAccount for wallet with Id ({}) not found!!", walletChannel.getId());
            alertService.send("walletAccount for wallet with Id (" + walletChannel.getId() + ") not found!!");
            throw new ServiceException("walletAccount for wallet with Id (" + walletChannel.getId() + ") not found!!", RequestService.GENERAL_ERROR);
        }

        return walletAccount;
    }
}
