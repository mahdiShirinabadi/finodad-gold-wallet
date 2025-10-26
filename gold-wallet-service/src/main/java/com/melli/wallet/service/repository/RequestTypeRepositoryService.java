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
	String CREATE_COLLATERAL = "create_collateral";
	String RELEASE_COLLATERAL = "release_collateral";
	String INCREASE_COLLATERAL = "increase_collateral";
	String SEIZE_COLLATERAL = "seize_collateral";
	String SELL_COLLATERAL = "sell_collateral";
	RequestTypeEntity getRequestType(String name);
    RequestTypeEntity getRequestTypeById(long id);
	void clearCache (long id)throws InternalServiceException;
	void clearCacheAllData();
}
