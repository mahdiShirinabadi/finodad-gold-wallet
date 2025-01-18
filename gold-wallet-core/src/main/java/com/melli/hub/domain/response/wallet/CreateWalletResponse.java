package com.melli.hub.domain.response.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class CreateWalletResponse {

	@JsonProperty("walletId")
	private String walletId;

	@JsonProperty("mobile")
	private String mobile;

	@JsonProperty("nationalCode")
	private String nationalCode;

	@JsonProperty("walletAccountObject")
	private List<WalletAccountObject> walletAccountObjectList;
}
