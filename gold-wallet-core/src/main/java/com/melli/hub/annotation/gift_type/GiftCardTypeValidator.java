package com.melli.hub.annotation.gift_type;

import com.melli.hub.domain.enumaration.GiftCardStepStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;

@Log4j2
public class GiftCardTypeValidator implements ConstraintValidator<GiftCardTypeValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return Arrays.stream(GiftCardStepStatus.values()).map(GiftCardStepStatus::getText).toList().contains(value);
    }
}
