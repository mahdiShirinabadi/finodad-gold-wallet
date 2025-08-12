package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.RrnExtraData;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.RequestTypeEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import com.melli.wallet.domain.master.persistence.RrnRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.RrnRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Class Name: RrnServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class RrnRepositoryServiceImplementation implements RrnRepositoryService {

    private final RrnRepository rrnRepository;

    @Override
    public RrnEntity generateTraceId(String nationalCode, ChannelEntity channelEntity, RequestTypeEntity requestTypeEntity, String accountNumber, String amount) throws InternalServiceException {

        log.info("start generate traceId ===> mobile({}), channel({})", nationalCode, channelEntity.getUsername() );
        try{
            RrnEntity rrnEntity = new RrnEntity();
            rrnEntity.setChannel(channelEntity);
            rrnEntity.setNationalCode(nationalCode);
            rrnEntity.setCreatedAt(new Date());
            rrnEntity.setCreatedBy(channelEntity.getUsername());
            rrnEntity.setRequestTypeEntity(requestTypeEntity);
            rrnEntity.setExtraData(new RrnExtraData(amount, accountNumber));
            rrnEntity = rrnRepository.save(rrnEntity);
            return rrnRepository.findById(rrnEntity.getId());
        }catch (Exception e){
            log.error("error in save traceId, and get error ===> ({})", e.getMessage());
            throw new InternalServiceException("error in generate traceId ===> " + e.getMessage(), StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }
    }

    @Override
    public RrnEntity findByUid(String uuid) throws InternalServiceException {
        RrnEntity rrn = rrnRepository.findByUuid(uuid);
        if(rrn == null){
            log.error("rrn for uuid ({}) not found", uuid);
            throw new InternalServiceException("rrn not exist in table !!! ", StatusRepositoryService.UUID_NOT_FOUND, HttpStatus.OK);
        }
        return rrn;
    }

    @Override
    public RrnEntity checkRrn(String uuid, ChannelEntity channelEntity, RequestTypeEntity requestTypeEntity, String amount, String accountNumber) throws InternalServiceException {
        RrnEntity rrn = findByUid(uuid);

        if(rrn.getChannel().getId() != channelEntity.getId()){
            String errorMessage = String.format("channelId of user and traceId (%s), are not the same !!!", uuid);
            log.error(errorMessage);
            throw new InternalServiceException(errorMessage, StatusRepositoryService.UUID_NOT_FOUND, HttpStatus.OK);
        }

        if(rrn.getRequestTypeEntity().getId() != requestTypeEntity.getId()){
            String errorMessage = String.format("request type for uuid (%s) is (%s)  and must be (%s), not valid", uuid, requestTypeEntity.getId(), rrn.getRequestTypeEntity().getId());
            log.error(errorMessage);
            throw new InternalServiceException(errorMessage, StatusRepositoryService.UUID_NOT_FOUND, HttpStatus.OK);
        }

        if(StringUtils.hasText(amount)){
            String storedAmount = rrn.getExtraData().getAmount();
            if(!amount.equals(storedAmount)){
                String errorMessage = String.format("price: (%s) mismatch for RRN UUID: %s with amount (%s)", amount, uuid, storedAmount);
                log.error(errorMessage);
                throw new InternalServiceException(errorMessage, StatusRepositoryService.PRICE_NOT_SAME_WITH_UUID, HttpStatus.OK);
            }
        }

        if(StringUtils.hasText(accountNumber)){
            String storedAccountNumber = rrn.getExtraData().getAccountNumber();
            if(!accountNumber.equals(storedAccountNumber)){
                String errorMessage = String.format("Account number: (%s) mismatch for RRN UUID: %s with account (%s)", accountNumber, uuid, storedAccountNumber);
                log.error(errorMessage);
                throw new InternalServiceException(errorMessage, StatusRepositoryService.ACCOUNT_NUMBER_NOT_SAME_WITH_UUID, HttpStatus.OK);
            }
        }

        return rrn;
    }

    @Override
    public RrnEntity findRrnById(long id) throws InternalServiceException {
        RrnEntity rrn = rrnRepository.findById(id);
        if(rrn == null){
            throw new InternalServiceException("rrn ===> " + id + " not exist in table !!! ", StatusRepositoryService.UUID_NOT_FOUND, HttpStatus.OK);
        }
        return rrn;
    }
}
