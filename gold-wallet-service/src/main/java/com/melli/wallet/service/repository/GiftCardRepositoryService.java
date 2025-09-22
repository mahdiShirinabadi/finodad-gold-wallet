package com.melli.wallet.service.repository;

import com.melli.wallet.domain.enumaration.GiftCardStepStatus;
import com.melli.wallet.domain.master.entity.GiftCardEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Class Name: GiftCardOperationService
 * Author: Mahdi Shirinabadi
 * Date: 9/22/2025
 */
public interface GiftCardRepositoryService {

    void save(GiftCardEntity giftCardEntity) throws InternalServiceException;
    Optional<GiftCardEntity> findByRrnId(long rrnId);
    Optional<GiftCardEntity> findByUniqueCode(String uniqueCode);
    Optional<GiftCardEntity> findByUniqueCodeAndQuantityAndStatus(String uniqueCode, BigDecimal quantity, GiftCardStepStatus giftCardStepStatus) throws InternalServiceException;
    Long countByActiveCode(@Param("activeCode") String activeCode);
}
