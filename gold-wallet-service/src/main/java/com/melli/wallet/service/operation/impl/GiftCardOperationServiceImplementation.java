package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.GiftCardPaymentObjectDTO;
import com.melli.wallet.domain.dto.GiftCardProcessObjectDTO;
import com.melli.wallet.domain.dto.P2pObjectDTO;
import com.melli.wallet.domain.enumaration.GiftCardStepStatus;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.giftcard.GiftCardTrackResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardUuidResponse;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.domain.response.p2p.P2pUuidResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.*;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.date.DateUtils;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Class Name: GiftCardOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 9/21/2025
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class GiftCardOperationServiceImplementation implements GiftCardOperationService {

    private final RedisLockService redisLockService;
    private final RrnRepositoryService rrnRepositoryService;
    private final RequestRepositoryService requestRepositoryService;
    private final Helper helper;
    private final WalletRepositoryService walletRepositoryService;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final WalletGiftCardLimitationRepositoryService walletGiftCardLimitationRepositoryService;
    private final WalletGiftCardLimitationOperationService walletGiftCardLimitationOperationService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final TemplateRepositoryService templateRepositoryService;
    private final TransactionRepositoryService transactionRepositoryService;
    private final MessageResolverOperationService messageResolverOperationService;
    private final StatusRepositoryService statusRepositoryService;
    private final WalletAccountTypeRepositoryService walletAccountTypeRepositoryService;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final GiftCardRepositoryService giftCardRepositoryService;
    private final WalletOperationalService walletOperationalService;
    private final Random random = new Random();

    @Override
    public GiftCardUuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber, String currency) throws InternalServiceException {
        try {

            WalletAccountEntity walletAccount = walletAccountRepositoryService.findByAccountNumber(accountNumber);
            if(walletAccount == null) {
                log.error("wallet account not found for number ({})", walletAccount);
                throw new InternalServiceException("wallet account with number ({}) not exist", StatusRepositoryService.DST_ACCOUNT_NUMBER_NOT_FOUND, HttpStatus.OK);
            }

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, nationalCode, walletAccountRepositoryService, accountNumber);
            walletGiftCardLimitationOperationService.checkGeneral(channelEntity, walletAccountEntity.getWalletEntity(), new BigDecimal(amount), walletAccountEntity);
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.GIFT_CARD), accountNumber, amount);
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);

            return helper.fillGiftCardUuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    public P2pUuidResponse payment(GiftCardPaymentObjectDTO giftCardPaymentObjectDTO) throws InternalServiceException {
        return null;
    }

    @Override
    @Transactional
    public void process(GiftCardProcessObjectDTO giftCardProcessObjectDTO) throws InternalServiceException {

        log.info("start generate giftCard for nationalCode ({})", giftCardProcessObjectDTO.getNationalCode());

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.GIFT_CARD);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(giftCardProcessObjectDTO.getUniqueIdentifier());

        String key = giftCardProcessObjectDTO.getNationalCode();
        redisLockService.runAfterLock(key, this.getClass(), ()->{
            log.info("start checking existence of traceId({}) ...", giftCardProcessObjectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(giftCardProcessObjectDTO.getUniqueIdentifier(), giftCardProcessObjectDTO.getChannelEntity(), requestTypeEntity, String.valueOf(giftCardProcessObjectDTO.getQuantity()), giftCardProcessObjectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", giftCardProcessObjectDTO.getUniqueIdentifier());

            requestRepositoryService.findGiftCardDuplicateWithRrnId(rrnEntity.getId());

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, giftCardProcessObjectDTO.getAccountNumber());

            walletGiftCardLimitationOperationService.checkDailyLimitation(giftCardProcessObjectDTO.getChannelEntity(), walletAccountEntity.getWalletEntity(),
                    new BigDecimal(giftCardProcessObjectDTO.getQuantity()), walletAccountEntity, giftCardProcessObjectDTO.getUniqueIdentifier());

            CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(giftCardProcessObjectDTO.getChannelEntity(),
                    walletAccountEntity.getWalletEntity().getMobile(), walletAccountEntity.getWalletEntity().getNationalCode(),
                    WalletTypeRepositoryService.NORMAL_USER, List.of(WalletAccountCurrencyRepositoryService.GOLD),
                    List.of(WalletAccountTypeRepositoryService.GIFT_CARD));

            GiftCardEntity giftCardEntity = new GiftCardEntity();
            giftCardEntity.setActiveCode(generateScratchCode(15));
            giftCardEntity.setQuantity(new BigDecimal(rrnEntity.getExtraData().getAmount()));
            giftCardEntity.setRrnEntity(rrnEntity);
            giftCardEntity.setWalletAccountEntity(walletAccountEntity);
            giftCardEntity.setStatus(GiftCardStepStatus.INITIAL);
            giftCardEntity.setActivatedAt(null);
            giftCardEntity.setActivatedBy(null);
            giftCardEntity.setDestinationWalletAccountEntity(null);
            giftCardEntity.setWalletAccountCurrencyEntity(walletAccountEntity.getWalletAccountCurrencyEntity());
            giftCardEntity.setExpireAt(DateUtils.getNNextDayReturnDate(new Date(), 360));
            giftCardEntity.setNationalCodeBy(giftCardEntity.getNationalCodeBy());
            giftCardEntity.setCreatedBy(walletAccountEntity.getWalletEntity().getNationalCode());
            giftCardEntity.setCommission(giftCardProcessObjectDTO.getCommission());
            giftCardEntity.setCreatedAt(new Date());
            WalletAccountEntity giftWalletAccount = walletAccountRepositoryService.findByAccountNumber(createWalletResponse.getWalletAccountObjectList().getFirst().getAccountNumber());
            giftCardEntity.setGiftWalletAccountEntity(giftWalletAccount);

            try {
                giftCardRepositoryService.save(giftCardEntity);
            } catch (Exception ex) {
                log.error("error in save cashIn with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }


            log.info("start block amount for giftCard for uniqueIdentifier ({})", rrnEntity.getUuid());
            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.GIFT_CARD_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.GIFT_CARD_WITHDRAWAL);

            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("srcAccountNumber", walletAccountEntity.getAccountNumber());
            model.put("amount", giftCardEntity.getQuantity().add(giftCardEntity.getCommission()));
            // user first withdrawal (currency)
            log.info("start giftCard transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", rrnEntity.getUuid(), giftCardEntity.getQuantity(), walletAccountEntity.getId());

            TransactionEntity userFirstWithdrawal = createTransaction(
                    walletAccountEntity, giftCardEntity.getQuantity().add(giftCardEntity.getCommission()),
                    messageResolverOperationService.resolve(depositTemplate, model), giftCardProcessObjectDTO.getAdditionalData(), requestTypeEntity, rrnEntity);
            transactionRepositoryService.insertWithdraw(userFirstWithdrawal);
            log.info("finish giftCard transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal walletAccountId ({})", rrnEntity.getUuid(), giftCardEntity.getQuantity(), walletAccountEntity.getId());

            if (giftCardProcessObjectDTO.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {
                WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(giftCardProcessObjectDTO.getChannelEntity(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", rrnEntity.getUuid(), giftCardEntity.getCommission(), giftCardEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, giftCardEntity.getCommission(),
                        messageResolverOperationService.resolve(depositTemplate, model), giftCardProcessObjectDTO.getAdditionalData(), requestTypeEntity, rrnEntity);
                transactionRepositoryService.insertDeposit(commissionDeposit);
                log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", rrnEntity.getId(), giftCardEntity.getCommission(), giftCardEntity.getWalletAccountEntity().getWalletEntity().getNationalCode(), commissionDeposit.getId());
            }


            // user second deposit (currency)
            log.info("start giftCard transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({})", rrnEntity.getUuid(), giftCardEntity.getQuantity(), giftWalletAccount.getId());
            TransactionEntity giftCardDeposit = createTransaction(giftWalletAccount,
                    giftCardEntity.getQuantity(), messageResolverOperationService.resolve(withdrawalTemplate, model), giftCardProcessObjectDTO.getAdditionalData(), requestTypeEntity, rrnEntity);
            transactionRepositoryService.insertDeposit(giftCardDeposit);
            log.info("finish giftCard transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({})", rrnEntity.getUuid(), giftCardEntity.getQuantity(), giftWalletAccount.getId());


            log.info("Start updating CashInLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletGiftCardLimitationOperationService.updateLimitation(walletAccountEntity, giftCardEntity.getQuantity(), rrnEntity.getUuid());
            log.info("updating CashInLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());
            return null;
        }, giftCardProcessObjectDTO.getUniqueIdentifier());
    }


    @Override
    public GiftCardTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.GIFT_CARD);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uuid);
        rrnRepositoryService.checkRrn(uuid, channelEntity, requestTypeEntity, "", "");
        Optional<GiftCardEntity> entity = giftCardRepositoryService.findByRrnId(rrnEntity.getId());
        return helper.fillP2pTrackResponse(entity, statusRepositoryService);
    }

    private String generateScratchCode(int number) {
        while (true) {
            String scratchCode = generateRandomString(number);
            Long countRecord = giftCardRepositoryService.countByActiveCode(scratchCode);
            if (countRecord == 0) {
                return scratchCode;
            }
        }
    }

    private String generateRandomString(int number) {
        SettingGeneralEntity settingEntityRandomString = settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.GIFT_CARD_RANDOM_STRING);
        String saltChars =
                settingEntityRandomString != null ? settingEntityRandomString.getValue() : "ABCDEFGHJKMNLPQRSTUVWXYZ23456789-&$#@";
        StringBuilder salt = new StringBuilder();
        while (salt.length() < number) { // length of the random string.
            int index = random.nextInt(saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

    private TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
                                                String additionalData, RequestTypeEntity requestTypeEntity,
                                                RrnEntity rrn) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setWalletAccountEntity(account);
        transaction.setDescription(description);
        transaction.setAdditionalData(additionalData);
        transaction.setRequestTypeId(requestTypeEntity.getId());
        transaction.setRrnEntity(rrn);
        return transaction;
    }


    private WalletAccountEntity findChannelCommissionAccount(ChannelEntity channel, String walletAccountTypeName) throws InternalServiceException {
        List<WalletAccountEntity> accounts = walletAccountRepositoryService.findByWallet(channel.getWalletEntity());
        if (accounts.isEmpty()) {
            log.error("No wallet accounts found for channel {}", channel.getUsername());
            throw new InternalServiceException("na wallet account found for channel", StatusRepositoryService.CHANNEL_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        WalletAccountTypeEntity wageType = walletAccountTypeRepositoryService.findByNameManaged(WalletAccountTypeRepositoryService.WAGE);
        if (wageType == null) {
            log.error("Wallet account type wage not found for channel {}", channel.getUsername());
            throw new InternalServiceException("Wallet account type wage not found", StatusRepositoryService.WALLET_ACCOUNT_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        return accounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equalsIgnoreCase(walletAccountTypeName)
                        && x.getWalletAccountTypeEntity().getName().equalsIgnoreCase(wageType.getName())).findFirst().orElseThrow(() -> {
                    log.error("Commission account not found for channel {}", channel.getUsername());
                    return new InternalServiceException("Commission account not found", StatusRepositoryService.CHANNEL_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
                });
    }
}
