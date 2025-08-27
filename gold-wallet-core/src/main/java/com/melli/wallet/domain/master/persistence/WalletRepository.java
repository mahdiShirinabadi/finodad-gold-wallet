package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.WalletEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface WalletRepository extends CrudRepository<WalletEntity, Long> {

	WalletEntity findByNationalCodeAndWalletTypeEntityIdAndEndTimeIsNull(String nationalCode, long walletTypeEntityId);

	WalletEntity findByNationalCodeAndIdAndEndTimeIsNotNull(String nationalCode,Long id);
	WalletEntity findByNationalCodeAndId(String nationalCode,Long id);

	WalletEntity findByNationalCodeAndStatus(String nationalCode, WalletStatusEnum status);

	WalletEntity findById(long id);

	List<WalletEntity> findAllByStatus(WalletStatusEnum status);

	WalletEntity findByIdAndEndTimeIsNotNull(long walletId);


	@Query(value="select count(*) from wallet where mobile like '9%'", nativeQuery = true)
	long countWalletIsActive ();


	@Query(value="select count(*) from wallet \n" +
			"where date(create_time)>= :fromDate and date(create_time)<= :toDate", nativeQuery = true)
	long countWalletByDate (Date fromDate,Date toDate);

	WalletEntity findByNationalCode(String nationalCode);

	List<WalletEntity>findAllByMobileLike(String mobileLike);

	WalletEntity findByMobile(String mobile);
	@Query(value="SELECT * from wallet_account as wa\n" +
			"INNER JOIN wallet as w on wa.wallet_id = w.id\n" +
			"where wa.account_number= :accountNumber", nativeQuery = true)
	WalletEntity findWalletByAccountNumber(String accountNumber);

	List<WalletEntity> findByStatusAndNationalCodeContainingAndMobileContaining(WalletStatusEnum status, String nationalCode, String mobile);
}
