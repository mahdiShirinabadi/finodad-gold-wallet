package com.melli.wallet.service.operation;

import com.melli.wallet.domain.dto.CashOutObjectDTO;
import com.melli.wallet.domain.dto.PhysicalCashOutObjectDTO;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutTrackResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutTotalQuantityResponse;
import com.melli.wallet.exception.InternalServiceException;


public interface CashOutOperationService {

	UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber) throws InternalServiceException;
	CashOutResponse withdrawal(CashOutObjectDTO cashOutObjectDTO) throws InternalServiceException;
	CashOutTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp)throws InternalServiceException;

	UuidResponse physicalGenerateUuid(ChannelEntity channelEntity, String nationalCode, String quantity, String accountNumber, String currency) throws InternalServiceException;
	PhysicalCashOutResponse physicalWithdrawal(PhysicalCashOutObjectDTO physicalCashOutObjectDTO) throws InternalServiceException;
	PhysicalCashOutTrackResponse physicalInquiry(ChannelEntity channelEntity, String uuid, String channelIp)throws InternalServiceException;
	PhysicalCashOutTotalQuantityResponse calculateTotalQuantity(ChannelEntity channelEntity) throws InternalServiceException;
}
