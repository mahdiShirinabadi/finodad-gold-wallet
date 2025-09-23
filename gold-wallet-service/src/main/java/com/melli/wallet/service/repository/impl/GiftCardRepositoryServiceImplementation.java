package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.enumaration.GiftCardStepStatus;
import com.melli.wallet.domain.master.entity.GiftCardEntity;
import com.melli.wallet.domain.master.persistence.GiftCardRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.GiftCardRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Class Name: GiftCardOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 9/22/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class GiftCardRepositoryServiceImplementation implements GiftCardRepositoryService {

    private final GiftCardRepository giftCardRepository;


    @Override
    public void save(GiftCardEntity giftCardEntity) throws InternalServiceException {
        log.info("start save giftCard with uniqueCode ({})", giftCardEntity.getActiveCode());
        giftCardRepository.save(giftCardEntity);
    }

    @Override
    public Optional<GiftCardEntity> findByRrnId(long rrnId) {
        return giftCardRepository.findByRrnEntity_Id(rrnId);
    }

    @Override
    public Optional<GiftCardEntity> findByUniqueCode(String uniqueCode) {
        return giftCardRepository.findByActiveCode(uniqueCode);
    }

    @Override
    public Optional<GiftCardEntity> findByUniqueCodeAndQuantityAndStatus(String uniqueCode, BigDecimal quantity, GiftCardStepStatus giftCardStepStatus) {
        return giftCardRepository.findByActiveCodeAndQuantityAndStatus(uniqueCode, quantity, giftCardStepStatus);
    }

    @Override
    public Long countByActiveCode(String activeCode) {
        return giftCardRepository.countByActiveCode(activeCode);
    }
}

