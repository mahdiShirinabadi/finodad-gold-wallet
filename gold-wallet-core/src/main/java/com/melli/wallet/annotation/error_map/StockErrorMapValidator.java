package com.melli.wallet.annotation.error_map;

import com.melli.wallet.domain.enumaration.ErrorMessageTypeEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;

@Log4j2
public class StockErrorMapValidator implements ConstraintValidator<StockErrorMapValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return Arrays.stream(ErrorMessageTypeEnum.values()).map(ErrorMessageTypeEnum::getCode).toList().contains(value);
    }
}
