package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.dto.AggregationCashOutDTO;
import com.melli.wallet.domain.dto.AggregationGiftCardPaymentDTO;
import com.melli.wallet.domain.master.entity.GiftCardPaymentRequestEntity;
import com.melli.wallet.domain.master.entity.PhysicalCashOutRequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface GiftCardPaymentRequestRepository extends CrudRepository<GiftCardPaymentRequestEntity, Long> {

    GiftCardPaymentRequestEntity findById(long requestId);

    @Query(value = "select COALESCE(SUM(p.quantity), 0) as sumQuantity, count(*) as countRecord from {h-schema}gift_card_payment_request p inner join {h-schema}request r on p.request_id = r.id  where p.wallet_account_id in :walletAccountId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0", nativeQuery = true)
    AggregationGiftCardPaymentDTO findSumAmountBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
}
