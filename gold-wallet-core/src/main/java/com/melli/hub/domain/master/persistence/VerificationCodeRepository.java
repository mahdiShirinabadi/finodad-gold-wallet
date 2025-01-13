package com.melli.hub.domain.master.persistence;


import com.melli.hub.domain.enumaration.VerificationCodeEnum;
import com.melli.hub.domain.master.entity.VerificationCodeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface VerificationCodeRepository extends CrudRepository<VerificationCodeEntity, Long> {

	@Query("select e from VerificationCodeEntity e where e.nationalCode=:nationalCode and e.code = :code and e.status = :status and e.verificationCodeEnum = :verificationCodeEnum order by e.id desc limit 1")
	VerificationCodeEntity findByNationalCodeAndCodeAndStatusAndVerificationCodeEnumOrderByIdDesc(@Param("nationalCode") String nationalCode, @Param("code") String code, @Param("status") int status,
																											@Param("verificationCodeEnum") VerificationCodeEnum verificationCodeEnum);


	@Query("select e from VerificationCodeEntity e where e.nationalCode=:nationalCode and  e.status = :status and e.verificationCodeEnum = :verificationCodeEnum order by e.id desc limit 1")
	VerificationCodeEntity findByNationalCodeAndStatusAndVerificationCodeEnumOrderByIdDesc(@Param("nationalCode") String nationalCode, @Param("status") int status,
																								  @Param("verificationCodeEnum") VerificationCodeEnum verificationCodeEnum);

	void deleteAllByNationalCodeAndVerificationCodeEnum(String nationalCode, VerificationCodeEnum verificationCodeEnum);
}
