package com.melli.wallet.annotation.fund_type;

import com.melli.wallet.domain.enumaration.PurchaseTypeEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;

@Log4j2
public class PurchaseTypeValidator implements ConstraintValidator<PurchaseTypeValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return Arrays.stream(PurchaseTypeEnum.values()).map(PurchaseTypeEnum::getText).toList().contains(value);
    }
}
