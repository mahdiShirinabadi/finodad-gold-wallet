package com.melli.wallet.service;

import com.melli.wallet.domain.dto.CashOutObjectDTO;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface CashOutService {

	UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber) throws InternalServiceException;
	CashOutResponse withdrawal(CashOutObjectDTO cashOutObjectDTO) throws InternalServiceException;
	CashOutTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp)throws InternalServiceException;
}
