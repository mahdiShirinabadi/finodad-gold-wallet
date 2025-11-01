package com.melli.wallet.service.transactional;

import com.melli.wallet.domain.dto.ChargeObjectDTO;
import com.melli.wallet.domain.dto.PurchaseObjectDto;
import com.melli.wallet.domain.enumaration.CashInPaymentTypeEnum;
import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.operation.WalletBuyLimitationOperationService;
import com.melli.wallet.service.operation.WalletSellLimitationOperationService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.operation.CashInOperationService;
import com.melli.wallet.util.Utility;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class Name: PurchaseTransactionalService
 * Author: Mahdi Shirinabadi
 * Date: 7/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class PurchaseTransactionalService {

    private final RrnRepositoryService rrnRepositoryService;
    private final RequestRepositoryService requestRepositoryService;
    private final Helper helper;
    private final TransactionRepositoryService transactionRepositoryService;
    private final MessageResolverOperationService messageResolverOperationService;
    private final TemplateRepositoryService templateRepositoryService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final WalletBuyLimitationOperationService walletBuyLimitationOperationService;
    private final WalletSellLimitationOperationService walletSellLimitationOperationService;
    private final CashInOperationService cashInOperationService;
    private final StockRepositoryService stockRepositoryService;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PurchaseResponse processSell(
            PurchaseObjectDto purchaseObjectDto
    ) throws InternalServiceException {
        RrnEntity rrn = validateTransaction(purchaseObjectDto.getChannel(), purchaseObjectDto.getUniqueIdentifier(), requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.SELL), purchaseObjectDto.getQuantity(), purchaseObjectDto.getUserCurrencyAccount().getAccountNumber());
        log.info("Transaction validation completed - rrnId: {}, uuid: {}", rrn.getId(), rrn.getUuid());
        PurchaseRequestEntity purchaseRequest = createPurchaseRequest(
                purchaseObjectDto, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.SELL), rrn, TransactionTypeEnum.SELL);
        log.info("Purchase request entity created - requestId: {}, price: {}, terminalAmount: {}, finalQuantity: {}", 
            purchaseRequest.getId(), purchaseRequest.getPrice(), purchaseRequest.getTerminalAmount(), purchaseRequest.getFinalQuantity());

        sellProcessTransactions(
                purchaseRequest, purchaseObjectDto.getUserRialAccount(), purchaseObjectDto.getUserCurrencyAccount(),
                purchaseObjectDto.getMerchantRialAccount(), purchaseObjectDto.getMerchantCurrencyAccount(), purchaseObjectDto.getChannelCommissionAccount(),
                purchaseObjectDto.getCommission());
        log.info("Sell transactions processed successfully");
        purchaseRequest.setResult(StatusRepositoryService.SUCCESSFUL);
        log.info("Purchase request status set to SUCCESSFUL");
        requestRepositoryService.save(purchaseRequest);
        log.info("Purchase request saved successfully");
        PurchaseResponse response = helper.fillPurchaseResponse(purchaseRequest);
        log.info("Response created successfully - response: {}", response);
        return response;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PurchaseResponse processBuy(
            PurchaseObjectDto purchaseObjectDto,
            String uuidForCharge,
            String refNumber,
            boolean directCharge
    ) throws InternalServiceException {

        BigDecimal price = purchaseObjectDto.getPrice();
        BigDecimal commission = purchaseObjectDto.getCommission();

        if(directCharge){
            log.info("Direct charge enabled - charging account for customer with nationalCode: {}, walletAccount: {}", 
                purchaseObjectDto.getNationalCode(), purchaseObjectDto.getUserRialAccount().getAccountNumber());
            BigDecimal totalChargeAmount = purchaseObjectDto.getPrice().add(commission);
            ChargeObjectDTO chargeObject = new ChargeObjectDTO(
                purchaseObjectDto.getChannel(), 
                purchaseObjectDto.getNationalCode(), 
                uuidForCharge,
                String.valueOf(totalChargeAmount), 
                refNumber, 
                purchaseObjectDto.getUserRialAccount().getAccountNumber(),
                purchaseObjectDto.getAdditionalData(),
                "0.0.0.0", 
                CashInPaymentTypeEnum.ACCOUNT_TO_ACCOUNT.getText()
            );
            cashInOperationService.charge(chargeObject);
            log.info("Direct charge completed successfully");
        }

        RrnEntity rrn = validateTransaction(purchaseObjectDto.getChannel(), purchaseObjectDto.getUniqueIdentifier(), requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.BUY), price, purchaseObjectDto.getUserCurrencyAccount().getAccountNumber());
        log.info("Transaction validation completed - rrnId: {}, uuid: {}", rrn.getId(), rrn.getUuid());
        PurchaseRequestEntity purchaseRequest = createPurchaseRequest(
                purchaseObjectDto, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.BUY), rrn, TransactionTypeEnum.BUY);
        log.info("Purchase request entity created - requestId: {}, price: {}, terminalAmount: {}, finalQuantity: {}", 
            purchaseRequest.getId(), purchaseRequest.getPrice(), purchaseRequest.getTerminalAmount(), purchaseRequest.getFinalQuantity());

        buyProcessTransactions(
                purchaseRequest, purchaseObjectDto.getUserRialAccount(), purchaseObjectDto.getUserCurrencyAccount(),
                purchaseObjectDto.getMerchantRialAccount(), purchaseObjectDto.getMerchantCurrencyAccount(), purchaseObjectDto.getChannelCommissionAccount(), purchaseObjectDto.getCommission());
        log.info("Buy transactions processed successfully");
        log.info("Updating buy limitations - uniqueIdentifier: {}, walletAccountId: {}", 
            purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getWalletAccount().getId());
        walletBuyLimitationOperationService.updateLimitation(purchaseObjectDto.getUserCurrencyAccount(), purchaseObjectDto.getPrice(), purchaseObjectDto.getQuantity(), purchaseObjectDto.getUniqueIdentifier(), purchaseRequest.getWalletAccount().getWalletAccountCurrencyEntity());
        log.info("Buy limitations updated successfully");
        BigDecimal netAmount = purchaseObjectDto.getPrice().subtract(purchaseObjectDto.getCommission());
        if(netAmount.compareTo(new BigDecimal("0")) <= 0){
            log.error("Commission validation failed - commission: {} is bigger than or equal to price: {}", 
                purchaseObjectDto.getCommission(), purchaseObjectDto.getPrice());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }
        log.info("Commission validation passed - netAmount: {}", netAmount);
        purchaseRequest.setResult(StatusRepositoryService.SUCCESSFUL);
        log.info("Purchase request status set to SUCCESSFUL");
        requestRepositoryService.save(purchaseRequest);
        log.info("Purchase request saved successfully");
        PurchaseResponse response = helper.fillPurchaseResponse(purchaseRequest);
        log.info("Response created successfully - response: {}", response);
        return response;
    }

    private RrnEntity validateTransaction(
            ChannelEntity channel, String uniqueIdentifier, RequestTypeEntity requestTypeEntity, BigDecimal amount, String accountNumber) throws InternalServiceException {
        log.info("Checking uniqueIdentifier {}", uniqueIdentifier);
        RrnEntity rrn = rrnRepositoryService.checkRrn(uniqueIdentifier, channel, requestTypeEntity, String.valueOf(amount), accountNumber);
        log.info("Checking traceId {}", rrn.getId());
        requestRepositoryService.checkTraceIdIsUnique(rrn.getId(), new PurchaseRequestEntity());
        return rrn;
    }

    private PurchaseRequestEntity createPurchaseRequest(
            PurchaseObjectDto purchaseObjectDto, RequestTypeEntity requestTypeEntity, RrnEntity rrnEntity, TransactionTypeEnum transactionTypeEnum

    ) throws InternalServiceException {
        PurchaseRequestEntity request = new PurchaseRequestEntity();
        request.setCreatedAt(new Date());
        request.setCreatedBy(purchaseObjectDto.getChannel().getUsername());
        request.setPrice(purchaseObjectDto.getPrice().longValue());
        if (TransactionTypeEnum.BUY.equals(transactionTypeEnum)) {
            request.setTerminalAmount(purchaseObjectDto.getPrice());
            //in buy we get a RIAL commission
            request.setFinalQuantity(purchaseObjectDto.getQuantity());
        }
        if (TransactionTypeEnum.SELL.equals(transactionTypeEnum)) {
            //in set we get a GOLD commission
            request.setTerminalAmount(purchaseObjectDto.getQuantity());
            request.setFinalQuantity(purchaseObjectDto.getQuantity());
        }
        request.setQuantity(purchaseObjectDto.getQuantity());
        request.setWalletAccount(purchaseObjectDto.getUserCurrencyAccount());
        request.setMerchantEntity(purchaseObjectDto.getMerchant());
        request.setNationalCode(purchaseObjectDto.getNationalCode());
        request.setRrnEntity(rrnEntity);
        request.setChannel(purchaseObjectDto.getChannel());
        request.setAdditionalData(purchaseObjectDto.getAdditionalData());
        request.setRequestTypeEntity(requestTypeEntity);
        request.setCommission(purchaseObjectDto.getCommission());
        request.setTransactionTypeEnum(transactionTypeEnum);
        requestRepositoryService.save(request);
        return request;
    }

    /**
     * 1) User sends: quantity, price, commission
     * 2) Database changes:
     * user: - (price + commission)
     * merchant: + quantity (gold sold)
     * channel: + commission (wage)
     * merchant: + price (cash received)
     * Example:
     User Rial Account: - (price + commission)
     User Gold Account: +quantity
     Merchant Rial Account: +price
     Merchant Gold Account: -quantity
     Channel Commission Account: +commission
     * channel: +500 RIAL commission
     * merchant: +10,000 RIAL
     * @param purchaseRequest
     * @param userRialAccount
     * @param userCurrencyAccount
     * @param merchantRialAccount
     * @param merchantCurrencyAccount
     * @param channelCommissionAccount
     * @param commission
     * @throws InternalServiceException
     */
    private void buyProcessTransactions(
            PurchaseRequestEntity purchaseRequest, WalletAccountEntity userRialAccount,
            WalletAccountEntity userCurrencyAccount, WalletAccountEntity merchantRialAccount,
            WalletAccountEntity merchantCurrencyAccount, WalletAccountEntity channelCommissionAccount
            , BigDecimal commission
    ) throws InternalServiceException {

        log.info("start buyProcessTransactions for uniqueIdentifier ({})", purchaseRequest.getRrnEntity().getUuid());

        String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.BUY_DEPOSIT);
        String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.BUY_WITHDRAWAL);

        Map<String, Object> model = new HashMap<>();
        model.put("traceId", String.valueOf(purchaseRequest.getRrnEntity().getId()));
        model.put("additionalData", purchaseRequest.getAdditionalData());
        model.put("amount", purchaseRequest.getQuantity());
        model.put("price", purchaseRequest.getPrice() + purchaseRequest.getCommission().longValue());
        model.put("merchant", purchaseRequest.getMerchantEntity().getName());
        model.put("nationalCode", Utility.maskNationalCode(purchaseRequest.getNationalCode()));

        // User withdrawal (rial)
        log.info("start buy transaction for uniqueIdentifier ({}), price ({}) for withdrawal user from nationalCode ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice() + purchaseRequest.getCommission().longValue(), purchaseRequest.getNationalCode(), userRialAccount.getId());
        TransactionEntity userWithdrawalTransaction = createTransaction(userRialAccount, BigDecimal.valueOf(purchaseRequest.getPrice()).add(purchaseRequest.getCommission()),
                messageResolverOperationService.resolve(withdrawalTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionRepositoryService.insertWithdraw(userWithdrawalTransaction);
        log.info("finish buy transaction for uniqueIdentifier ({}), price ({}) for withdrawal user from nationalCode ({}) with transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), purchaseRequest.getNationalCode(), userWithdrawalTransaction.getId());

        // commission must be rial
        if (commission.compareTo(BigDecimal.valueOf(0L)) > 0) {

            String commissionTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COMMISSION);
            Map<String, Object> modelCommission = new HashMap<>();
            model.put("traceId", String.valueOf(purchaseRequest.getRrnEntity().getId()));
            model.put("commission", purchaseRequest.getCommission());
            model.put("requestType", purchaseRequest.getRequestTypeEntity().getFaName());

            log.info("start buy transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), commission, purchaseRequest.getNationalCode(), channelCommissionAccount.getId());
            TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, commission,
                    messageResolverOperationService.resolve(commissionTemplate, modelCommission), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
            transactionRepositoryService.insertDeposit(commissionDeposit);
            log.info("finish buy transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", purchaseRequest.getRrnEntity().getId(), commission, purchaseRequest.getNationalCode(), commissionDeposit.getId());
        }

        // Merchant deposit (rial)
        BigDecimal merchantDepositPrice = BigDecimal.valueOf((purchaseRequest.getPrice()));
        log.info("start buy transaction for uniqueIdentifier ({}), price ({}), commission ({}), finalPrice ({}) for deposit merchant walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), commission, merchantDepositPrice, merchantRialAccount.getId());
        TransactionEntity merchantDeposit = createTransaction(
                merchantRialAccount, merchantDepositPrice,
                messageResolverOperationService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionRepositoryService.insertDeposit(merchantDeposit);
        log.info("finish buy transaction for uniqueIdentifier ({}), price ({}) for deposit merchant walletAccountId({}) with transactionId({})", purchaseRequest.getRrnEntity().getUuid(), merchantDepositPrice, merchantRialAccount.getId(), merchantDeposit.getId());

        // Merchant withdrawal (currency)
        log.info("start buy transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal merchant walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), merchantCurrencyAccount.getId());
        TransactionEntity merchantWithdrawal = createTransaction(
                merchantCurrencyAccount, (purchaseRequest.getQuantity()),
                messageResolverOperationService.resolve(withdrawalTemplate, model),
                purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity()
        );
        transactionRepositoryService.insertWithdraw(merchantWithdrawal);
        log.info("finish buy transaction for uniqueIdentifier ({}), quantity ({}) for withdrawal merchant walletAccountId({}), transactionId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), merchantCurrencyAccount.getId(), merchantWithdrawal.getId());

        // User deposit (currency)
        log.info("start buy transaction for uniqueIdentifier ({}), quantity ({}) for deposit currency user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), userCurrencyAccount.getId());
        TransactionEntity userDepositTransaction = createTransaction(
                userCurrencyAccount,
                purchaseRequest.getQuantity(), messageResolverOperationService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity()
        );
        transactionRepositoryService.insertDeposit(userDepositTransaction);
        log.info("finish buy transaction for uniqueIdentifier ({}), quantity ({}) for deposit currency user walletAccountId({}), transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), userCurrencyAccount.getId(), userDepositTransaction.getId());

        stockRepositoryService.insertDeposit(userDepositTransaction);

        log.info("terminate buyProcessTransactions for uniqueIdentifier ({})", purchaseRequest.getRrnEntity().getUuid());
    }

    private void sellProcessTransactions(
            PurchaseRequestEntity purchaseRequest, WalletAccountEntity userRialAccount,
            WalletAccountEntity userCurrencyAccount, WalletAccountEntity merchantRialAccount,
            WalletAccountEntity merchantCurrencyAccount, WalletAccountEntity channelCommissionAccount
            , BigDecimal commission
    ) throws InternalServiceException {
        log.info("start sellProcessTransactions for uniqueIdentifier ({})", purchaseRequest.getRrnEntity().getUuid());
        String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.SELL_DEPOSIT);
        String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.SELL_WITHDRAWAL);

        Map<String, Object> model = new HashMap<>();
        model.put("traceId", String.valueOf(purchaseRequest.getRrnEntity().getId()));
        model.put("additionalData", purchaseRequest.getAdditionalData());
        model.put("amount", purchaseRequest.getQuantity());
        model.put("price", purchaseRequest.getPrice());
        model.put("merchant", purchaseRequest.getMerchantEntity().getName());
        model.put("nationalCode", purchaseRequest.getNationalCode());

        // user withdrawal (currency) (quantity)
        log.info("start sell transaction for uniqueIdentifier ({}), quantity ({}) for user withdrawal user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), merchantCurrencyAccount.getId());
        TransactionEntity userCurrencyWithdrawal = createTransaction(userCurrencyAccount, purchaseRequest.getQuantity(),
                messageResolverOperationService.resolve(withdrawalTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionRepositoryService.insertWithdraw(userCurrencyWithdrawal);
        log.info("start sell transaction for uniqueIdentifier ({}), quantity ({}) for user withdrawal user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity(), merchantCurrencyAccount.getId());


        // merchant withdrawal (rial)
        log.info("start sell transaction for uniqueIdentifier ({}), price ({}) for merchant withdrawal from id ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), purchaseRequest.getMerchantEntity().getId(), userRialAccount.getId());

        TransactionEntity merchantRialWithdrawal = createTransaction(
                merchantRialAccount, BigDecimal.valueOf(purchaseRequest.getPrice()),
                messageResolverOperationService.resolve(withdrawalTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionRepositoryService.insertWithdraw(merchantRialWithdrawal);
        log.info("finish sell transaction for uniqueIdentifier ({}), price ({}) for merchant withdrawal from id ({}), walletAccountId ({}), transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), purchaseRequest.getMerchantEntity().getId(), userRialAccount.getId(),
                merchantRialWithdrawal.getId());

        // Channel commission deposit (if applicable)
        //commission type must be currency
        if (commission.compareTo(BigDecimal.valueOf(0L)) > 0) {
            log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", purchaseRequest.getRrnEntity().getUuid(), commission, purchaseRequest.getNationalCode(), channelCommissionAccount.getId());
            String commissionTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COMMISSION);
            Map<String, Object> modelCommission = new HashMap<>();
            model.put("traceId", String.valueOf(purchaseRequest.getRrnEntity().getId()));
            model.put("commission", purchaseRequest.getCommission());
            model.put("requestType", purchaseRequest.getRequestTypeEntity().getFaName());
            TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, commission,
                    messageResolverOperationService.resolve(commissionTemplate, modelCommission), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
            transactionRepositoryService.insertDeposit(commissionDeposit);
            log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", purchaseRequest.getRrnEntity().getId(), commission, purchaseRequest.getNationalCode(), commissionDeposit.getId());
        }

        // user deposit (rial) (price)
        log.info("start sell transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), userCurrencyAccount.getId());
        TransactionEntity userRialDeposit = createTransaction(userRialAccount, BigDecimal.valueOf(purchaseRequest.getPrice()),
                messageResolverOperationService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionRepositoryService.insertDeposit(userRialDeposit);
        log.info("finish sell transaction for uniqueIdentifier ({}), price ({}) for user deposit currency user walletAccountId({}), transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getPrice(), userCurrencyAccount.getId(), userRialDeposit.getId());



        // merchant deposit (currency) (quantity - commission)
        log.info("start sell transaction for uniqueIdentifier ({}), quantity ({}) for merchant deposit currency user walletAccountId({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity().subtract(commission), userCurrencyAccount.getId());
        TransactionEntity merchantCurrencyDeposit = createTransaction(merchantCurrencyAccount, purchaseRequest.getQuantity().subtract(purchaseRequest.getCommission()),
                messageResolverOperationService.resolve(depositTemplate, model), purchaseRequest.getAdditionalData(), purchaseRequest, purchaseRequest.getRrnEntity());
        transactionRepositoryService.insertDeposit(merchantCurrencyDeposit);
        log.info("finish sell transaction for uniqueIdentifier ({}), quantity ({}) for merchant deposit currency user walletAccountId({}), transactionId ({})", purchaseRequest.getRrnEntity().getUuid(), purchaseRequest.getQuantity().subtract(commission), userCurrencyAccount.getId(), merchantCurrencyDeposit.getId());
        walletSellLimitationOperationService.updateLimitation(userCurrencyAccount, new BigDecimal(purchaseRequest.getPrice()), purchaseRequest.getQuantity(), purchaseRequest.getRrnEntity().getUuid());
        stockRepositoryService.insertWithdraw(userCurrencyWithdrawal);
    }

    private TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
                                                String additionalData, PurchaseRequestEntity purchaseRequest,
                                                RrnEntity rrn) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setWalletAccountEntity(account);
        transaction.setDescription(description);
        transaction.setAdditionalData(additionalData);
        transaction.setRequestTypeId(purchaseRequest.getRequestTypeEntity().getId());
        transaction.setRrnEntity(rrn);
        return transaction;
    }

}
