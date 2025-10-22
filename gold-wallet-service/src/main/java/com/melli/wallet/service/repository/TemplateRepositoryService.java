package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.TemplateEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;


public interface TemplateRepositoryService {

	String P2P_DEPOSIT = "p2p_deposit";
	String P2P_WITHDRAWAL = "p2p_withdrawal";
	String CASH_IN = "cash_in";
	String CASH_OUT = "cash_out";
	String PHYSICAL_CASH_OUT = "physical_cash_out";
	String BUY_WITHDRAWAL = "BUY_WITHDRAWAL";
	String BUY_DEPOSIT = "BUY_DEPOSIT";

	String SELL_WITHDRAWAL = "SELL_WITHDRAWAL";
	String SELL_DEPOSIT = "SELL_DEPOSIT";

	String GIFT_CARD_WITHDRAWAL = "GIFT_CARD_WITHDRAWAL";
	String GIFT_CARD_DEPOSIT = "GIFT_CARD_DEPOSIT";

	String COLLATERAL_CREATE_WITHDRAWAL = "COLLATERAL_CREATE_WITHDRAWAL";
	String COLLATERAL_CREATE_DEPOSIT = "COLLATERAL_CREATE_DEPOSIT";

	String COLLATERAL_RELEASE_WITHDRAWAL = "COLLATERAL_RELEASE_WITHDRAWAL";
	String COLLATERAL_RELEASE_DEPOSIT = "COLLATERAL_RELEASE_DEPOSIT";

	String COLLATERAL_INCREASE_WITHDRAWAL = "COLLATERAL_INCREASE_WITHDRAWAL";
	String COLLATERAL_INCREASE_DEPOSIT = "COLLATERAL_INCREASE_DEPOSIT";

	String COLLATERAL_SEIZE_WITHDRAWAL = "COLLATERAL_SEIZE_WITHDRAWAL";
	String COLLATERAL_SEIZE_DEPOSIT = "COLLATERAL_SEIZE_DEPOSIT";

	String COLLATERAL_SELL_WITHDRAWAL = "COLLATERAL_SELL_WITHDRAWAL";
	String COLLATERAL_SELL_DEPOSIT = "COLLATERAL_SELL_DEPOSIT";

	String COLLATERAL_RETURN_AFTER_SELL_DEPOSIT = "COLLATERAL_RETURN_AFTER_SELL_DEPOSIT";

	String getTemplate(String name);

	TemplateEntity findById(Long id) throws InternalServiceException;

	TemplateEntity findByName(String name) throws InternalServiceException;

	void updateTemplate(TemplateEntity templateEntity) throws InternalServiceException;

    Page<TemplateEntity> findAllWithSpecification(Map<String, String> searchCriteria, Pageable pageable);
    
    void createTemplate(TemplateEntity templateEntity) throws InternalServiceException;

}
