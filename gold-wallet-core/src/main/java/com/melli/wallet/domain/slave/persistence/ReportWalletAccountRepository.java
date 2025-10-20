package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.slave.entity.ReportWalletAccountEntity;
import com.melli.wallet.domain.slave.entity.ReportWalletEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReportWalletAccountRepository extends CrudRepository<ReportWalletAccountEntity, Long> {

    List<ReportWalletAccountEntity> findByWalletEntity(ReportWalletEntity wallet);

    ReportWalletAccountEntity findTopByWalletEntityAndWalletAccountCurrencyEntityId(ReportWalletEntity wallet, long walletAccountCurrencyId);

    List<ReportWalletAccountEntity> findByWalletEntity(ReportWalletEntity wallet, Pageable pageable);

    ReportWalletAccountEntity findByWalletEntityAndAccountNumberAndEndTimeIsNull(ReportWalletEntity wallet, String account);

    ReportWalletAccountEntity findByAccountNumberAndEndTimeIsNull(String account);

    ReportWalletAccountEntity findByAccountNumber(String account);

    ReportWalletAccountEntity findById(long id);

    @Query(value = "select balance from {h-schema}wallet_account where id = :id", nativeQuery = true)
    BigDecimal getBalance(@Param("id") long id);

    @Modifying
    @Query(value = "update {h-schema}wallet_account set balance= balance + :amount where id = :id", nativeQuery = true)
    void increaseBalance(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query(value = "update {h-schema}wallet_account set balance= balance - :amount  where id = :id and (balance - :amount) >=0", nativeQuery = true)
    int decreaseBalance(@Param("id") long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query(value = "update {h-schema}wallet_account set block_amount= block_amount + :amount where id = :id", nativeQuery = true)
    void blockAmount(@Param("id") long id, @Param("amount") long amount);

    @Modifying
    @Query(value = "update {h-schema}wallet_account  set block_amount= block_amount - :amount where id = :id", nativeQuery = true)
    void unblockAmount(@Param("id") long id, @Param("amount") long amount);

    List<ReportWalletAccountEntity> findAllByStatus(WalletStatusEnum status);

    ReportWalletAccountEntity findFirstByWalletEntityAndWalletAccountTypeEntityId(ReportWalletEntity wallet, long accountTypeId);
    List<ReportWalletAccountEntity> findAllByWalletEntityAndWalletAccountTypeEntityId(ReportWalletEntity wallet, long accountTypeId);

    @Query(value = "SELECT w.balance AS balance, w.block_amount AS blockAmount  FROM {h-schema}wallet_account w where id=:id", nativeQuery = true)
    BalanceDTO getBalanceById(@Param("id") long id);

    @Query(value = "SELECT " +
            "wa.id as accountId, " +
            "wa.account_number as accountNumber, " +
            "wa.status as accountStatus, " +
            "wa.balance as balance, " +
            "wa.wallet_id as walletId, " +
            "wat.name as accountTypeName, " +
            "wac.name as currencyName " +
            "FROM {h-schema}wallet_account wa " +
            "INNER JOIN {h-schema}wallet_account_type wat ON wa.wallet_account_type_id = wat.id " +
            "INNER JOIN {h-schema}wallet_account_currency wac ON wa.wallet_account_currency_id = wac.id " +
            "WHERE wa.wallet_id IN :walletIds", nativeQuery = true)
    List<Object[]> findAccountDetailsByWalletIds(@Param("walletIds") List<Long> walletIds);

    @Query(value = "SELECT SUM(wa.balance) FROM {h-schema}wallet_account wa " +
                   "INNER JOIN {h-schema}wallet w ON wa.wallet_id = w.id " +
                   "WHERE w.wallet_type_id NOT IN :excludedWalletTypeIds " +
                   "AND wa.wallet_account_currency_id = :currencyId", nativeQuery = true)
    BigDecimal calculateTotalBalanceExcludingWalletTypeIdsAndCurrency(
            @Param("excludedWalletTypeIds") List<Long> excludedWalletTypeIds,
            @Param("currencyId") Long currencyId);
} 