package com.melli.hub.annotation.passport_number;

import com.melli.hub.util.PersianUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.TextUtils;

@Log4j2
public class PassportNumberValidator implements ConstraintValidator<PassportNumberValidation, String> {
    private static final String PASSPORT_NO_PATTERN = "^[A-Za-z]\\d{8}$";
    private boolean allowEmpty;


    @Override
    public void initialize(PassportNumberValidation passportNumberValidation) {
        allowEmpty = passportNumberValidation.allowEmpty();
    }

    public boolean isValid(String passportNumberInput, ConstraintValidatorContext cxt) {
        if (TextUtils.isEmpty(passportNumberInput)) {
            log.error("passportNumber ({}) is empty!!!", passportNumberInput);
            return allowEmpty;
        }
        return isValidPassportNumber(passportNumberInput);
    }


    private boolean isValidPassportNumber(String passportNumberInput) {
        String passportNumber = normalizePassportNumber(passportNumberInput);
        return passportNumber.matches(PASSPORT_NO_PATTERN);
    }

    private String normalizePassportNumber(String passportNumberInput) {
        return PersianUtils.fromPersianNumeric(passportNumberInput);
    }
}
