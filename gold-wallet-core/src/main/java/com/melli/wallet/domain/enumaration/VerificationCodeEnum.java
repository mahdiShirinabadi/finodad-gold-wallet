package com.melli.wallet.domain.enumaration;

import lombok.Getter;

@Getter
public enum VerificationCodeEnum {
    LOGIN("LOGIN"),
    REGISTER("REGISTER"),
    FORGET_PASSWORD("FORGET_PASSWORD");
    final String name;

    private VerificationCodeEnum(String statusId){
        this.name = statusId;
    }

    public String getStatusName(){
        return name;
    }


}
