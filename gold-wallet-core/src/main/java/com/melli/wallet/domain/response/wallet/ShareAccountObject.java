package com.melli.wallet.domain.response.wallet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ShareAccountObject {

    private String title;
    private String accountOwner;
    private String nationalCode;
    private String statusOwner;
}
