package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.WalletEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface WalletRepository extends CrudRepository<WalletEntity, Long> {

	WalletEntity findByNationalCodeAndWalletTypeEntityIdAndEndTimeIsNull(String nationalCode, long walletTypeEntityId);

	WalletEntity findByNationalCodeAndIdAndEndTimeIsNotNull(String nationalCode,Integer id);
	WalletEntity findByNationalCodeAndId(String nationalCode,Integer id);

	WalletEntity findByNationalCodeAndStatus(String nationalCode, int status);

	WalletEntity findById(Integer walletId);

	List<WalletEntity> findAllByStatus(int status);

	WalletEntity findByIdAndEndTimeIsNotNull(Integer walletId);


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
}
