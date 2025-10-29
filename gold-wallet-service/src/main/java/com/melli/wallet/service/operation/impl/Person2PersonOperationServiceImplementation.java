package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.P2pObjectDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.domain.response.p2p.P2pUuidResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.operation.Person2PersonOperationService;
import com.melli.wallet.service.operation.WalletP2pLimitationOperationService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.Utility;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
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
 * Class Name: CashServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class Person2PersonOperationServiceImplementation implements Person2PersonOperationService {

    private final RedisLockService redisLockService;
    private final RrnRepositoryService rrnRepositoryService;
    private final RequestRepositoryService requestRepositoryService;
    private final Helper helper;
    private final WalletRepositoryService walletRepositoryService;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final WalletP2pLimitationOperationService walletP2pLimitationOperationService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final TemplateRepositoryService templateRepositoryService;
    private final TransactionRepositoryService transactionRepositoryService;
    private final MessageResolverOperationService messageResolverOperationService;
    private final StatusRepositoryService statusRepositoryService;

    @Override
    public P2pUuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber, String destAccountNumber) throws InternalServiceException {
        log.info("=== P2P GENERATE UUID OPERATION START ===");
        log.info("Input parameters - nationalCode: {}, amount: {}, accountNumber: {}, destAccountNumber: {}, channel: {}", 
            nationalCode, amount, accountNumber, destAccountNumber, channelEntity.getUsername());

        try {
            log.info("=== ACCOUNT VALIDATION ===");
            if(accountNumber.equalsIgnoreCase(destAccountNumber)){
                log.error("Source and destination account validation failed - src: {}, dst: {}", accountNumber, destAccountNumber);
                throw new InternalServiceException("src and dst account are same in generate uuid", StatusRepositoryService.SRC_ACCOUNT_SAME_DST_ACCOUNT_NUMBER, HttpStatus.OK);
            }
            log.info("Source and destination account validation passed - accounts are different");

            log.debug("Searching for destination wallet account - accountNumber: {}", destAccountNumber);
            WalletAccountEntity destinationWalletAccount = walletAccountRepositoryService.findByAccountNumber(destAccountNumber);
            if(destinationWalletAccount == null) {
                log.error("Destination wallet account not found - accountNumber: {}", destAccountNumber);
                throw new InternalServiceException("wallet account with number ({}) not exist", StatusRepositoryService.DST_ACCOUNT_NUMBER_NOT_FOUND, HttpStatus.OK);
            }
            log.info("Destination wallet account found - accountId: {}, accountNumber: {}, nationalCode: {}", 
                destinationWalletAccount.getId(), destinationWalletAccount.getAccountNumber(), 
                destinationWalletAccount.getWalletEntity().getNationalCode());

            log.debug("Validating source wallet and account - nationalCode: {}, accountNumber: {}", nationalCode, accountNumber);
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, nationalCode, walletAccountRepositoryService, accountNumber);
            log.info("Source wallet account validated - accountId: {}, accountNumber: {}", 
                walletAccountEntity.getId(), walletAccountEntity.getAccountNumber());

            log.info("=== P2P LIMITATION CHECKS ===");
            BigDecimal transferAmount = new BigDecimal(amount);
            log.debug("Checking P2P limitations - amount: {}, accountId: {}", transferAmount, walletAccountEntity.getId());
            walletP2pLimitationOperationService.checkGeneral(channelEntity, walletAccountEntity.getWalletEntity(), transferAmount, walletAccountEntity);
            log.info("P2P limitation checks passed - amount: {}", transferAmount);

            log.info("=== GENERATING TRACE ID ===");
            log.info("Generating trace ID - username: {}, nationalCode: {}", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.P2P), accountNumber, amount);
            log.info("Trace ID generated successfully - uuid: {}, rrnId: {}, username: {}, nationalCode: {}", 
                rrnEntity.getUuid(), rrnEntity.getId(), channelEntity.getUsername(), nationalCode);

            log.info("=== CREATING RESPONSE ===");
            P2pUuidResponse response = helper.fillP2pUuidResponse(destinationWalletAccount.getWalletEntity().getNationalCode(), rrnEntity.getUuid());
            log.info("Response created - destNationalCode: {}, uuid: {}", 
                destinationWalletAccount.getWalletEntity().getNationalCode(), rrnEntity.getUuid());

            log.info("=== P2P GENERATE UUID OPERATION COMPLETED SUCCESSFULLY ===");
            return response;
        } catch (InternalServiceException e) {
            log.error("P2P generate UUID failed - username: {}, nationalCode: {}, error: {}", 
                channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void process(P2pObjectDTO p2pObjectDTO) throws InternalServiceException {

        log.info("=== P2P PROCESS OPERATION START ===");
        log.info("Input parameters - uniqueIdentifier: {}, srcAccountNumber: {}, destAccountNumber: {}, quantity: {}, commission: {}, nationalCode: {}", 
            p2pObjectDTO.getUniqueIdentifier(), p2pObjectDTO.getAccountNumber(), p2pObjectDTO.getDestAccountNumber(), 
            p2pObjectDTO.getQuantity(), p2pObjectDTO.getCommission(), p2pObjectDTO.getNationalCode());

        log.info("=== ACCOUNT VALIDATION ===");
        if(p2pObjectDTO.getAccountNumber().equalsIgnoreCase(p2pObjectDTO.getDestAccountNumber())){
            log.error("Source and destination account validation failed - src: {}, dst: {}", 
                p2pObjectDTO.getAccountNumber(), p2pObjectDTO.getDestAccountNumber());
            throw new InternalServiceException("src and dst account are same", StatusRepositoryService.SRC_ACCOUNT_SAME_DST_ACCOUNT_NUMBER, HttpStatus.OK);
        }
        log.info("Source and destination account validation passed - accounts are different");

        log.info("=== COMMISSION VALIDATION ===");
        BigDecimal netAmount = p2pObjectDTO.getQuantity().subtract(p2pObjectDTO.getCommission());
        log.debug("Calculating net amount - quantity: {}, commission: {}, netAmount: {}", 
            p2pObjectDTO.getQuantity(), p2pObjectDTO.getCommission(), netAmount);
        
        if(netAmount.compareTo(new BigDecimal("0")) <= 0){
            log.error("Commission validation failed - commission: {} is bigger than or equal to quantity: {}", 
                p2pObjectDTO.getCommission(), p2pObjectDTO.getQuantity());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }
        log.info("Commission validation passed - netAmount: {}", netAmount);

        log.info("=== REQUEST TYPE AND RRN VALIDATION ===");
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.P2P);
        log.debug("Request type retrieved - type: {}", requestTypeEntity.getName());
        
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(p2pObjectDTO.getUniqueIdentifier());
        log.debug("RRN entity found - rrnId: {}, uuid: {}", rrnEntity.getId(), rrnEntity.getUuid());

        log.info("Starting Redis lock acquisition for account: {}", p2pObjectDTO.getAccountNumber());
        redisLockService.runWithLockUntilCommit(p2pObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("=== LOCK ACQUIRED - STARTING P2P PROCESS CRITICAL SECTION ===");
            log.info("Checking existence of traceId: {}", p2pObjectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(p2pObjectDTO.getUniqueIdentifier(), p2pObjectDTO.getChannel(), requestTypeEntity, String.valueOf(p2pObjectDTO.getQuantity()), p2pObjectDTO.getAccountNumber());
            log.info("Trace ID validation completed successfully");

            log.debug("Checking for duplicate P2P requests with rrnId: {}", rrnEntity.getId());
            requestRepositoryService.findP2pDuplicateWithRrnId(rrnEntity.getId());
            log.debug("No duplicate P2P requests found");

            log.info("=== DESTINATION ACCOUNT VALIDATION ===");
            log.debug("Searching for destination wallet account - accountNumber: {}", p2pObjectDTO.getDestAccountNumber());
            WalletAccountEntity destinationWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(p2pObjectDTO.getDestAccountNumber());

            if(destinationWalletAccountEntity == null) {
                log.error("Destination wallet account not found - accountNumber: {}", p2pObjectDTO.getDestAccountNumber());
                throw new InternalServiceException("wallet account with number ({}) not exist", StatusRepositoryService.DST_ACCOUNT_NUMBER_NOT_FOUND, HttpStatus.OK);
            }
            log.info("Destination wallet account found - accountId: {}, accountNumber: {}, nationalCode: {}", 
                destinationWalletAccountEntity.getId(), destinationWalletAccountEntity.getAccountNumber(), 
                destinationWalletAccountEntity.getWalletEntity().getNationalCode());

            log.info("=== SOURCE ACCOUNT VALIDATION ===");
            log.debug("Validating source wallet and account - nationalCode: {}, accountNumber: {}", 
                rrnEntity.getNationalCode(), p2pObjectDTO.getAccountNumber());
            WalletAccountEntity srcWalletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, p2pObjectDTO.getAccountNumber());
            log.info("Source wallet account validated - accountId: {}, accountNumber: {}", 
                srcWalletAccountEntity.getId(), srcWalletAccountEntity.getAccountNumber());

            log.info("=== CURRENCY VALIDATION ===");
            log.debug("Checking currency compatibility - srcCurrencyId: {}, dstCurrencyId: {}", 
                srcWalletAccountEntity.getWalletAccountCurrencyEntity().getId(), 
                destinationWalletAccountEntity.getWalletAccountCurrencyEntity().getId());
            
            if(srcWalletAccountEntity.getWalletAccountCurrencyEntity().getId() != destinationWalletAccountEntity.getWalletAccountCurrencyEntity().getId()){
                log.error("Currency validation failed - src and dst currencies are different - srcCurrency: {}, dstCurrency: {}", 
                    srcWalletAccountEntity.getWalletAccountCurrencyEntity().getName(), 
                    destinationWalletAccountEntity.getWalletAccountCurrencyEntity().getName());
                throw new InternalServiceException("src and dst account are same", StatusRepositoryService.CURRENCY_SRC_ACCOUNT_SAME_DST_ACCOUNT_NUMBER, HttpStatus.OK);
            }
            log.info("Currency validation passed - both accounts use same currency: {}", 
                srcWalletAccountEntity.getWalletAccountCurrencyEntity().getName());

            log.info("=== P2P DAILY LIMITATION CHECKS ===");
            log.debug("Checking P2P daily limitations - quantity: {}, srcAccountId: {}", 
                p2pObjectDTO.getQuantity(), srcWalletAccountEntity.getId());
            walletP2pLimitationOperationService.checkDailyLimitation(p2pObjectDTO.getChannel(), srcWalletAccountEntity.getWalletEntity(),
                    p2pObjectDTO.getQuantity(), srcWalletAccountEntity, p2pObjectDTO.getUniqueIdentifier());
            log.info("P2P daily limitation checks passed - quantity: {}", p2pObjectDTO.getQuantity());

            log.info("=== CREATING P2P REQUEST ENTITY ===");
            log.debug("Creating P2P request entity");
            Person2PersonRequestEntity requestEntity = new Person2PersonRequestEntity();
            requestEntity.setAmount(p2pObjectDTO.getQuantity());
            requestEntity.setFinalAmount(requestEntity.getAmount().add(p2pObjectDTO.getCommission()));
            requestEntity.setSourceAccountWalletEntity(srcWalletAccountEntity);
            requestEntity.setRrnEntity(rrnEntity);
            requestEntity.setChannel(p2pObjectDTO.getChannel());
            requestEntity.setResult(StatusRepositoryService.CREATE);
            requestEntity.setChannelIp(p2pObjectDTO.getIp());
            requestEntity.setRequestTypeEntity(requestTypeEntity);
            requestEntity.setCreatedBy(p2pObjectDTO.getChannel().getUsername());
            requestEntity.setCreatedAt(new Date());
            requestEntity.setDestinationAccountWalletEntity(destinationWalletAccountEntity);
            requestEntity.setCommission(p2pObjectDTO.getCommission());
            log.info("P2P request entity created - amount: {}, finalAmount: {}, commission: {}, srcAccount: {}, dstAccount: {}", 
                requestEntity.getAmount(), requestEntity.getFinalAmount(), requestEntity.getCommission(),
                requestEntity.getSourceAccountWalletEntity().getAccountNumber(), 
                requestEntity.getDestinationAccountWalletEntity().getAccountNumber());

            log.info("=== SAVING P2P REQUEST ===");
            try {
                log.debug("Saving P2P request entity to database");
                requestRepositoryService.save(requestEntity);
                log.info("P2P request entity saved successfully - requestId: {}", requestEntity.getId());
            } catch (Exception ex) {
                log.error("Failed to save P2P request - error: {}", ex.getMessage());
                throw new InternalServiceException("error in save P2P", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            
            requestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            requestEntity.setAdditionalData(p2pObjectDTO.getAdditionalData());
            log.info("P2P request status set to SUCCESSFUL");

            log.info("=== PROCESSING P2P TRANSACTIONS ===");
            log.info("Starting transaction processing for uniqueIdentifier: {}", requestEntity.getRrnEntity().getUuid());
            
            log.debug("Retrieving transaction templates");
            String depositTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.P2P_DEPOSIT);
            String withdrawalTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.P2P_WITHDRAWAL);
            log.debug("Templates retrieved - deposit: {}, withdrawal: {}", depositTemplate, withdrawalTemplate);

            log.debug("Preparing transaction model");
            Map<String, Object> model = new HashMap<>();
            model.put("traceId", String.valueOf(requestEntity.getRrnEntity().getId()));
            model.put("srcNationalCode", Utility.maskNationalCode(requestEntity.getSourceAccountWalletEntity().getWalletEntity().getNationalCode()));
            model.put("amount", requestEntity.getAmount());
            model.put("dstNationalCode", Utility.maskNationalCode(requestEntity.getDestinationAccountWalletEntity().getWalletEntity().getNationalCode()));
            log.debug("Transaction model prepared - traceId: {}, srcAccount: {}, amount: {}, dstAccount: {}", 
                model.get("traceId"), model.get("srcAccountNumber"), model.get("amount"), model.get("dstAccountNumber"));
            log.info("=== CREATING SOURCE WITHDRAWAL TRANSACTION ===");
            BigDecimal totalWithdrawalAmount = requestEntity.getAmount().add(requestEntity.getCommission());
            log.info("Creating source withdrawal transaction - uniqueIdentifier: {}, amount: {}, commission: {}, totalAmount: {}, walletAccountId: {}", 
                requestEntity.getRrnEntity().getUuid(), requestEntity.getAmount(), requestEntity.getCommission(), 
                totalWithdrawalAmount, srcWalletAccountEntity.getId());

            TransactionEntity userFirstWithdrawal = createTransaction(
                    srcWalletAccountEntity, totalWithdrawalAmount,
                    messageResolverOperationService.resolve(depositTemplate, model), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());
            
            log.debug("Executing source withdrawal transaction - transactionId: {}, amount: {}", 
                userFirstWithdrawal.getId(), userFirstWithdrawal.getAmount());
            transactionRepositoryService.insertWithdraw(userFirstWithdrawal);
            log.info("Source withdrawal transaction completed successfully - transactionId: {}, amount: {}", 
                userFirstWithdrawal.getId(), userFirstWithdrawal.getAmount());

            if (requestEntity.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {

                String commissionTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COMMISSION);
                Map<String, Object> modelCommission = new HashMap<>();
                model.put("traceId", String.valueOf(requestEntity.getRrnEntity().getId()));
                model.put("commission", requestEntity.getCommission());
                model.put("requestType", requestEntity.getRequestTypeEntity().getFaName());

                log.info("=== PROCESSING COMMISSION TRANSACTION ===");
                log.debug("Commission amount greater than zero - processing commission deposit");
                WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(p2pObjectDTO.getChannel(), WalletAccountCurrencyRepositoryService.GOLD);
                log.info("Channel commission account found - accountId: {}, accountNumber: {}", 
                    channelCommissionAccount.getId(), channelCommissionAccount.getAccountNumber());
                
                log.info("Creating commission deposit transaction - uniqueIdentifier: {}, commission: {}, walletAccountId: {}", 
                    requestEntity.getRrnEntity().getUuid(), requestEntity.getCommission(), channelCommissionAccount.getId());
                
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, requestEntity.getCommission(),
                        messageResolverOperationService.resolve(commissionTemplate, modelCommission), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());
                
                log.debug("Executing commission deposit transaction - transactionId: {}, amount: {}", 
                    commissionDeposit.getId(), commissionDeposit.getAmount());
                transactionRepositoryService.insertDeposit(commissionDeposit);
                log.info("Commission deposit transaction completed successfully - transactionId: {}, commission: {}", 
                    commissionDeposit.getId(), requestEntity.getCommission());
            } else {
                log.info("No commission processing required - commission amount: {}", requestEntity.getCommission());
            }

            log.info("=== CREATING DESTINATION DEPOSIT TRANSACTION ===");
            log.info("Creating destination deposit transaction - uniqueIdentifier: {}, amount: {}, walletAccountId: {}", 
                requestEntity.getRrnEntity().getUuid(), requestEntity.getAmount(), requestEntity.getDestinationAccountWalletEntity().getId());
            
            TransactionEntity userSecondDeposit = createTransaction(destinationWalletAccountEntity, requestEntity.getAmount(),
                    messageResolverOperationService.resolve(withdrawalTemplate, model), requestEntity.getAdditionalData(), requestEntity, requestEntity.getRrnEntity());
            
            log.debug("Executing destination deposit transaction - transactionId: {}, amount: {}", 
                userSecondDeposit.getId(), userSecondDeposit.getAmount());
            transactionRepositoryService.insertDeposit(userSecondDeposit);
            log.info("Destination deposit transaction completed successfully - transactionId: {}, amount: {}", 
                userSecondDeposit.getId(), userSecondDeposit.getAmount());

            log.info("=== FINALIZING P2P REQUEST ===");
            log.debug("Saving final P2P request with updated status - requestId: {}", requestEntity.getId());
            requestRepositoryService.save(requestEntity);
            log.info("Final P2P request saved successfully");

            log.info("=== UPDATING P2P LIMITATIONS ===");
            log.info("Updating P2P limitations for walletAccount: {}", srcWalletAccountEntity.getAccountNumber());
            walletP2pLimitationOperationService.updateLimitation(srcWalletAccountEntity, p2pObjectDTO.getQuantity(), p2pObjectDTO.getUniqueIdentifier());
            log.info("P2P limitations updated successfully for walletAccount: {}", srcWalletAccountEntity.getAccountNumber());
            
            log.info("=== P2P PROCESS OPERATION COMPLETED SUCCESSFULLY ===");
            return null;
        }, p2pObjectDTO.getUniqueIdentifier());
    }


    @Override
    public P2pTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        log.info("=== P2P INQUIRY OPERATION START ===");
        log.info("Input parameters - uuid: {}, channel: {}, channelIp: {}", 
            uuid, channelEntity.getUsername(), channelIp);

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.P2P);
        log.debug("Request type retrieved - type: {}", requestTypeEntity.getName());
        
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uuid);
        log.debug("RRN entity found - rrnId: {}, uuid: {}", rrnEntity.getId(), rrnEntity.getUuid());
        
        log.debug("Validating RRN for inquiry - uuid: {}, channel: {}", uuid, channelEntity.getUsername());
        rrnRepositoryService.checkRrn(uuid, channelEntity, requestTypeEntity, "", "");
        log.info("RRN validation passed for inquiry");
        
        log.debug("Retrieving P2P request with rrnId: {}", rrnEntity.getId());
        Person2PersonRequestEntity entity = requestRepositoryService.findP2pWithRrnId(rrnEntity.getId());
        log.info("P2P request found - requestId: {}, amount: {}, status: {}", 
            entity.getId(), entity.getAmount(), entity.getResult());
        
        log.info("=== CREATING INQUIRY RESPONSE ===");
        P2pTrackResponse response = helper.fillP2pTrackResponse(entity, statusRepositoryService);
        log.info("Inquiry response created - status: {}, amount: {}", 
            response.getResult(), response.getQuantity());
        
        log.info("=== P2P INQUIRY OPERATION COMPLETED ===");
        return response;
    }


    private TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
                                                String additionalData, RequestEntity requestEntity,
                                                RrnEntity rrn) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setWalletAccountEntity(account);
        transaction.setDescription(description);
        transaction.setAdditionalData(additionalData);
        transaction.setRequestTypeId(requestEntity.getRequestTypeEntity().getId());
        transaction.setRrnEntity(rrn);
        return transaction;
    }

}
