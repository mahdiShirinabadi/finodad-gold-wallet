package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.dto.BalanceObjectDTO;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WalletAccountRepository extends CrudRepository<WalletAccountEntity, Long> {

    List<WalletAccountEntity> findByWalletEntity(WalletEntity wallet);

    WalletAccountEntity findTopByWalletEntityAndWalletAccountCurrencyEntityId(WalletEntity wallet, long walletAccountCurrencyId);

    List<WalletAccountEntity> findByWalletEntity(WalletEntity wallet, Pageable pageable);

    WalletAccountEntity findByWalletEntityAndAccountNumberAndEndTimeIsNull(WalletEntity wallet, String account);

    WalletAccountEntity findByAccountNumberAndEndTimeIsNull(String account);

    WalletAccountEntity findByAccountNumber(String account);

    WalletAccountEntity findById(int id);

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

    List<WalletAccountEntity> findAllByStatus(int status);

    WalletAccountEntity findFirstByWalletEntityAndWalletAccountTypeEntityId(WalletEntity wallet, int accountTypeId);
    List<WalletAccountEntity> findAllByWalletEntityAndWalletAccountTypeEntityId(WalletEntity wallet, int accountTypeId);

    @Query(value = "SELECT w.balance AS balance, w.block_amount AS blockAmount  FROM {h-schema}wallet_account w where id=:id", nativeQuery = true)
    BalanceObjectDTO getBalanceById(@Param("id") long id);
}
