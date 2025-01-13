package com.melli.hub.ipg;


import com.melli.hub.ChannelException;
import com.melli.hub.ipg.response.SadedVerifyResponse;

import java.net.URISyntaxException;

public interface SadadPspChannelInterface {

	String SADAD_FAIL = "-1";
	String SADAD_OK = "0";
	String SADAD_INVALID_MERCHANT = "3";
	String TIME_OUT = "998";
	String GENERAL_ERROR = "999";


	String requestToken(String uuid, String merchantId, String terminalId, long amount,
						long orderId, String returnUrl, String additionalData,
						String merchantKey, String mobileNumber, String nationalCode) throws ChannelException, URISyntaxException;

	String verify(String uuid, String token, String merchantKey) throws ChannelException, URISyntaxException;

}
