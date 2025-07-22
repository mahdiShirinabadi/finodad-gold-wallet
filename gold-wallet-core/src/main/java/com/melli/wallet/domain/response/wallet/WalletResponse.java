package com.melli.wallet.domain.response.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class WalletResponse {

	@Schema(name = NamingProperty.ID)
	@JsonProperty(NamingProperty.ID)
	private String walletId;

	@Schema(name = NamingProperty.MOBILE_NUMBER)
	@JsonProperty(NamingProperty.MOBILE_NUMBER)
	private String mobile;

	@Schema(name = NamingProperty.NATIONAL_CODE)
	@JsonProperty(NamingProperty.NATIONAL_CODE)
	private String nationalCode;

	@Schema(name = NamingProperty.STATUS)
	@JsonProperty(NamingProperty.STATUS)
	private String status;

	@Schema(name = NamingProperty.STATUS_DESCRIPTION)
	@JsonProperty(NamingProperty.STATUS_DESCRIPTION)
	private String statusDescription;

	@Schema(name = NamingProperty.WALLET_ACCOUNT_OBJECT_LIST)
	@JsonProperty(NamingProperty.WALLET_ACCOUNT_OBJECT_LIST)
	private List<WalletAccountObject> walletAccountObjectList;
}
