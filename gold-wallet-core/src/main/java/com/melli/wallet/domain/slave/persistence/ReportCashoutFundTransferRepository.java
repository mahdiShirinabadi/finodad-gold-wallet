package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.domain.master.entity.CashoutFundTransferEntity;
import com.melli.wallet.domain.master.entity.FundTransferAccountToAccountRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportCashoutFundTransferEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Class Name: CashoutFundTransferRepository
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 * this class use for when we want multi cashout in one fundTransfer
 */
@Repository
public interface ReportCashoutFundTransferRepository extends CrudRepository<ReportCashoutFundTransferEntity, Long> {


    @Query(value = "SELECT * FROM {h-schema}cash_out_fund_transfer WHERE cash_out_request_id = (:id)", nativeQuery = true)
    List<ReportCashoutFundTransferEntity> findAllByCashoutRequestEntityId(@Param("id") long id);
}
