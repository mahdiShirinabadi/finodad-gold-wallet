package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.TemplateEntity;

import java.util.List;


public interface TemplateService {

	String CASH_IN = "cash_in";
	String PURCHASE_WITHDRAWAL = "purchase_withdrawal";
	String PURCHASE_DEPOSIT = "purchase_deposit";

	String getTemplate(String name);

	List<TemplateEntity> findAllTemplate();

}
