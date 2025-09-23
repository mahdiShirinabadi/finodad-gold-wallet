package com.melli.wallet.domain.request.wallet.physical;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import com.melli.wallet.domain.request.wallet.CommissionObject;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PhysicalCashOutWalletRequestJson {

    @StringValidation
    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER, description = "شناسه یکتا")
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @NumberValidation(label = NamingProperty.QUANTITY ,allowDecimal = true)
    @Schema(name = NamingProperty.QUANTITY, description = "مقدار ارز")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @NationalCodeValidation(label = NamingProperty.NATIONAL_CODE)
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "اطلاعات تکمیلی")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @StringValidation(label = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;


    @Schema(name = NamingProperty.SIGN, description = "امضا دیجیتال")
    @JsonProperty(NamingProperty.SIGN)
    private String sign;

    @Schema(name = NamingProperty.COMMISSION_OBJECT, description = "کارمزد")
    @JsonProperty(NamingProperty.COMMISSION_OBJECT)
    private CommissionObject commissionObject;

    @StringValidation(label = NamingProperty.CURRENCY)
    @Schema(name = NamingProperty.CURRENCY, description = "نوع ارز (مثال: GOLD, SILVER, PLATINUM)", example = "GOLD")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

    @Hidden
    public String getDataString() {
        return uniqueIdentifier + "|" + quantity + "|" + nationalCode;
    }
}
