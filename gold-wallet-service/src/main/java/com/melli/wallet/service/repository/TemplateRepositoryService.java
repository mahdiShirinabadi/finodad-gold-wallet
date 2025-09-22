package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.TemplateEntity;

import java.util.List;


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

	String getTemplate(String name);

	List<TemplateEntity> findAllTemplate();

}
