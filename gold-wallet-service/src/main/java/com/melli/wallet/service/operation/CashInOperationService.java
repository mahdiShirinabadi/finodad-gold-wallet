package com.melli.wallet.service.operation;

import com.melli.wallet.domain.dto.ChargeObjectDTO;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface CashInOperationService {

	UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber) throws InternalServiceException;
	CashInResponse charge(ChargeObjectDTO chargeObjectDTO) throws InternalServiceException;
	CashInTrackResponse inquiry(ChannelEntity channel, String uuid, String channelIp)throws InternalServiceException;
}
