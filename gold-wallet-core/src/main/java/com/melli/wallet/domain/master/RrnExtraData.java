package com.melli.wallet.domain.master;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Class Name: RrnExtraData
 * Author: Mahdi Shirinabadi
 * Date: 4/30/2025
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RrnExtraData implements Serializable {

    private String amount;
    private String accountNumber;
}
