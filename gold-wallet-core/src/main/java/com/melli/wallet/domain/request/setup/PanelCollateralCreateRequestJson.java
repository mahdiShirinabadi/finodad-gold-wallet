package com.melli.wallet.domain.request.setup;

import lombok.Getter;
import lombok.Setter;

/**
 * Class Name: ChannelCreateRequestJson
 * Author: Mahdi Shirinabadi
 * Date: 7/28/2025
 */
@Getter
@Setter
public class PanelCollateralCreateRequestJson {
    private String name;
    private String mobileNumber;
    private String economicCode;
    private String iban;
}
