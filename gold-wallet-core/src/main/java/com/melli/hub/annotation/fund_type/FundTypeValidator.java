package com.melli.hub.annotation.fund_type;

import com.melli.hub.domain.enumaration.FundTypeEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;

@Log4j2
public class FundTypeValidator implements ConstraintValidator<FundTypeValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return Arrays.stream(FundTypeEnum.values()).map(FundTypeEnum::getText).toList().contains(value);
    }
}
