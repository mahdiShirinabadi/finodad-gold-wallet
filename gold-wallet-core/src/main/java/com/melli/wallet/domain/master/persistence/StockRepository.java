package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.StockEntity;
import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StockRepository extends CrudRepository<StockEntity, Long> {

    StockEntity findByCodeAndWalletAccountCurrencyEntity(String code, WalletAccountCurrencyEntity walletAccountCurrencyEntity);

    @Modifying
    @Query(value = "update {h-schema}stock set balance= balance + :amount where id = :id", nativeQuery = true)
    void increaseBalance(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query(value = "update {h-schema}stock set balance= balance - :amount  where id = :id and (balance - :amount) >=0", nativeQuery = true)
    int decreaseBalance(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Query(value = "select balance from {h-schema}stock where id = :id", nativeQuery = true)
    BigDecimal getBalance(@Param("id") long id);

    @Query(value = "select COALESCE(SUM(balance), 0) as balance, wallet_account_currency_id  from {h-schema}stock group by wallet_account_currency_id", nativeQuery = true)
    List<AggregationStockByCurrencyDTO> getSumBalance();

    @Query(value = "select id, balance, code from {h-schema}stock where wallet_account_currency_id = :walletAccountCurrencyId", nativeQuery = true)
    List<AggregationStockDTO> getAllBalance(@Param("walletAccountCurrencyId") long walletAccountCurrencyId);

    public interface AggregationStockDTO {
        String getId();
        String getBalance();
        String getCode();
    }

    public interface AggregationStockByCurrencyDTO {
        String getBalance();
        String getCurrency();
    }
}
