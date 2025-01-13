package com.melli.hub.annotation.number;

import com.melli.hub.util.PersianUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.TextUtils;

@Log4j2
public class NumberValidator implements ConstraintValidator<NumberValidation, String> {
    private static final String NUMBER_PATTERN = "^\\d+$";
    private static final String NEGATIVE_NUMBER_PATTERN = "^-?\\d+$";
    private boolean allowEmpty;
    private boolean allowNegative;

    @Override
    public void initialize(NumberValidation numberValidation) {
        allowEmpty = numberValidation.allowEmpty();
        allowNegative = numberValidation.allowNegative();
    }

    public boolean isValid(String inputNumber, ConstraintValidatorContext cxt) {
        String number = PersianUtils.fromPersianNumeric(inputNumber);
        if (allowEmpty && TextUtils.isEmpty(number)) {
            return true;
        }

        if (TextUtils.isEmpty(number)) {
            log.error("number ({}) is empty!!!", number);
            return false;
        }
        String numberPattern = allowNegative ? NEGATIVE_NUMBER_PATTERN : NUMBER_PATTERN;
        if (number.matches(numberPattern)) {
            return true;
        }
        log.error("number ({}) is not match with regx ({})", number, numberPattern);
        return false;
    }
}
