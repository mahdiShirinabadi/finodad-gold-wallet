package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.domain.master.entity.CashoutFundTransferEntity;
import com.melli.wallet.domain.master.entity.FundTransferAccountToAccountRequestEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


/**
 * Class Name: CashoutFundTransferRepository
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 * this class use for when we want multi cashout in one fundTransfer
 */
@Repository
public interface CashoutFundTransferRepository extends CrudRepository<CashoutFundTransferEntity, Long> {

    List<CashoutFundTransferEntity> findAllByCashoutRequestEntity(CashOutRequestEntity cashOutRequestEntity);
    List<CashoutFundTransferEntity> findAllByFundTransferAccountToAccountRequestEntity(FundTransferAccountToAccountRequestEntity fundTransferAccountToAccountRequestEntity);

}
