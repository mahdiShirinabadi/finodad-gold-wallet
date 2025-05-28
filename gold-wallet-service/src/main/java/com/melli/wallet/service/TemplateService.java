package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.TemplateEntity;

import java.util.List;


public interface TemplateService {

	String CASH_IN = "cash_in";
	String CASH_OUT = "cash_out";
	String BUY_WITHDRAWAL = "BUY_WITHDRAWAL";
	String BUY_DEPOSIT = "BUY_DEPOSIT";

	String SELL_WITHDRAWAL = "SELL_WITHDRAWAL";
	String SELL_DEPOSIT = "SELL_DEPOSIT";

	String getTemplate(String name);

	List<TemplateEntity> findAllTemplate();

}
