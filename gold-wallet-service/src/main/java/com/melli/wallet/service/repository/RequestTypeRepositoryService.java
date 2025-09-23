package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.RequestTypeEntity;
import com.melli.wallet.exception.InternalServiceException;

/**
 * Created by shirinabadi on 03/11/2016.
 */
public interface RequestTypeRepositoryService {
	String GIFT_CARD = "gift_card";
	String P2P = "p2p";
	String CASH_IN = "cash_in";
	String CASH_OUT = "cash_out";
	String PHYSICAL_CASH_OUT = "physical_cash_out";
	String BUY = "buy";
	String SELL = "sell";
	String MERCHANT_INCREASE_BALANCE = "merchant_increase_balance";
	String MERCHANT_DECREASE_BALANCE = "merchant_decrease_balance";
	RequestTypeEntity getRequestType(String name);
	void clearCache (long id)throws InternalServiceException;
	void clearCacheAllData();
}
