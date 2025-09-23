package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.dto.AggregationGiftCardDTO;
import com.melli.wallet.domain.dto.AggregationP2PDTO;
import com.melli.wallet.domain.enumaration.GiftCardStepStatus;
import com.melli.wallet.domain.master.entity.GiftCardEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface GiftCardRepository extends CrudRepository<GiftCardEntity, Long> {

    Optional<GiftCardEntity> findByRrnEntity_Id(Long rrnId);
    Optional<GiftCardEntity> findByActiveCodeAndQuantityAndStatus(String uniqueCode, BigDecimal quantity, GiftCardStepStatus status);
    Optional<GiftCardEntity> findByActiveCode(String uniqueCode);
    @Query("select count(a.id) from GiftCardEntity a where a.activeCode = :activeCode")
    Long countByActiveCode(@Param("activeCode") String activeCode);

    @Query(value = "select COALESCE(SUM(p.quantity), 0) as sumQuantity, count(*) as countRecord from {h-schema}gift_card p where p.wallet_account_id in :walletAccountId" +
            " and date(p.created_at) BETWEEN date(:fromDate) AND  date(:toDate)", nativeQuery = true)
    AggregationGiftCardDTO findSumAmountBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
}
