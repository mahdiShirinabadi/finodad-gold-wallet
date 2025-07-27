package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.dto.BalanceObjectDTO;
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

    ReportWalletAccountEntity findById(int id);

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

    List<ReportWalletAccountEntity> findAllByStatus(int status);

    ReportWalletAccountEntity findFirstByWalletEntityAndWalletAccountTypeEntityId(ReportWalletEntity wallet, int accountTypeId);
    List<ReportWalletAccountEntity> findAllByWalletEntityAndWalletAccountTypeEntityId(ReportWalletEntity wallet, int accountTypeId);

    @Query(value = "SELECT w.balance AS balance, w.block_amount AS blockAmount  FROM {h-schema}wallet_account w where id=:id", nativeQuery = true)
    BalanceObjectDTO getBalanceById(@Param("id") long id);
} 