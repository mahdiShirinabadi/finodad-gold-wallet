package com.melli.hub.annotation.limit_number;

import com.melli.hub.util.PersianUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.TextUtils;

import java.util.List;

@Log4j2
public class LimitCharacterValidator implements ConstraintValidator<LimitCharacterValidation, String> {

    private boolean allowEmpty;
    private String allowedValues;


    @Override
    public void initialize(LimitCharacterValidation limitCharacterValidation) {
        allowEmpty = limitCharacterValidation.allowEmpty();
        allowedValues = limitCharacterValidation.allowedValues();
    }

    public boolean isValid(String inputValue, ConstraintValidatorContext cxt) {

        String value = PersianUtils.fromPersianNumeric(inputValue);

        if (allowEmpty && TextUtils.isEmpty(value)) {
            return true;
        }

        if (TextUtils.isEmpty(value)) {
            return false;
        }

        boolean hasText = false;

        List<String> validArray = List.of(allowedValues.split(";"));
        for (String row : validArray) {
            if (row.equalsIgnoreCase(value)) {
                hasText = true;
                break;
            }
        }
        if (!hasText) {
            log.error("value ({}) is not match with allowed values ({})", inputValue, validArray);
            return false;
        }
        return true;
    }
}
