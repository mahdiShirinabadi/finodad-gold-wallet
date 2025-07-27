package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportWalletEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportWalletRepository extends CrudRepository<ReportWalletEntity, Long> {

	ReportWalletEntity findByNationalCodeAndWalletTypeEntityIdAndEndTimeIsNull(String nationalCode, long walletTypeEntityId);

	ReportWalletEntity findByNationalCodeAndIdAndEndTimeIsNotNull(String nationalCode,Integer id);
	ReportWalletEntity findByNationalCodeAndId(String nationalCode,Integer id);

	ReportWalletEntity findByNationalCodeAndStatus(String nationalCode, int status);

	ReportWalletEntity findById(Integer walletId);

	List<ReportWalletEntity> findAllByStatus(int status);

	ReportWalletEntity findByIdAndEndTimeIsNotNull(Integer walletId);

	@Query(value="select count(*) from wallet where mobile like '9%'", nativeQuery = true)
	long countWalletIsActive ();

	@Query(value="select count(*) from wallet \n" +
			"where date(create_time)>= :fromDate and date(create_time)<= :toDate", nativeQuery = true)
	long countWalletByDate (Date fromDate,Date toDate);

	ReportWalletEntity findByNationalCode(String nationalCode);

	List<ReportWalletEntity>findAllByMobileLike(String mobileLike);

	ReportWalletEntity findByMobile(String mobile);
	
	@Query(value="SELECT * from wallet_account as wa\n" +
			"INNER JOIN wallet as w on wa.wallet_id = w.id\n" +
			"where wa.account_number= :accountNumber", nativeQuery = true)
	ReportWalletEntity findWalletByAccountNumber(String accountNumber);
} 