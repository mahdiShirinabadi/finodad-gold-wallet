package com.melli.wallet.domain.response.giftcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GiftCardTrackResponse {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private long  id;

    @Schema(name = NamingProperty.NATIONAL_CODE)
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.QUANTITY)
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER)
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @Schema(name = NamingProperty.RESULT)
    @JsonProperty(NamingProperty.RESULT)
    private int result;

    @Schema(name = NamingProperty.DESCRIPTION)
    @JsonProperty(NamingProperty.DESCRIPTION)
    private String description;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String walletAccountNumber;

    @Schema(name = NamingProperty.DEST_WALLET_ACCOUNT_NUMBER)
    @JsonProperty(NamingProperty.DEST_WALLET_ACCOUNT_NUMBER)
    private String destWalletAccountNumber;

    @Schema(name = NamingProperty.DEST_NATIONAL_CODE)
    @JsonProperty(NamingProperty.DEST_NATIONAL_CODE)
    private String destNationalCode;

    @Schema(name = NamingProperty.CREATE_TIME)
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createTime;

    @Schema(name = NamingProperty.CREATE_TIMESTAMP)
    @JsonProperty(NamingProperty.CREATE_TIMESTAMP)
    private long createTimeTimestamp;
}
