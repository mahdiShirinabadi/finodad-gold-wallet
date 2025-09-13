package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.slave.entity.ReportWalletEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportWalletRepository extends CrudRepository<ReportWalletEntity, Long>, JpaSpecificationExecutor<ReportWalletEntity> {

	ReportWalletEntity findByNationalCodeAndWalletTypeEntityIdAndEndTimeIsNull(String nationalCode, long walletTypeEntityId);

	ReportWalletEntity findByNationalCodeAndIdAndEndTimeIsNotNull(String nationalCode,long id);
	ReportWalletEntity findByNationalCodeAndId(String nationalCode,long id);

	ReportWalletEntity findByNationalCodeAndStatus(String nationalCode, WalletStatusEnum status);

	ReportWalletEntity findById(long walletId);

	List<ReportWalletEntity> findAllByStatus(WalletStatusEnum status);

	ReportWalletEntity findByIdAndEndTimeIsNotNull(long walletId);

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

	@Query(value = "SELECT w.* FROM {h-schema}wallet w " +
			"WHERE (:nationalCode IS NULL OR w.national_code = :nationalCode) " +
			"AND (:mobile IS NULL OR w.mobile LIKE CONCAT('%', :mobile, '%')) " +
			"AND (:fromTimeStr IS NULL OR w.created_at >= TO_TIMESTAMP(:fromTimeStr, 'YYYY-MM-DD HH24:MI:SS')) " +
			"AND (:toTimeStr IS NULL OR w.created_at <= TO_TIMESTAMP(:toTimeStr, 'YYYY-MM-DD HH24:MI:SS')) and  w.wallet_type_id = (select id from {h-schema}wallet_type where name='NORMAL_USER')" +
			"ORDER BY w.id DESC",
			countQuery = "SELECT COUNT(w.id) FROM {h-schema}wallet w " +
					"WHERE (:nationalCode IS NULL OR w.national_code = :nationalCode) " +
					"AND (:mobile IS NULL OR w.mobile LIKE CONCAT('%', :mobile, '%')) " +
					"AND (:fromTimeStr IS NULL OR w.created_at >= TO_TIMESTAMP(:fromTimeStr, 'YYYY-MM-DD HH24:MI:SS')) " +
					"AND (:toTimeStr IS NULL OR w.created_at <= TO_TIMESTAMP(:toTimeStr, 'YYYY-MM-DD HH24:MI:SS')) and w.wallet_type_id = (select id from {h-schema}wallet_type where name='NORMAL_USER')",
			nativeQuery = true)
	Page<ReportWalletEntity> findWalletsWithFilters(
			@Param("nationalCode") String nationalCode,
			@Param("mobile") String mobile,
			@Param("fromTimeStr") String fromTimeStr,
			@Param("toTimeStr") String toTimeStr,
			Pageable pageable);
} 