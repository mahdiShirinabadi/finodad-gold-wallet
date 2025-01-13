package com.melli.hub.annotation.string;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.TextUtils;

@Log4j2
public class StringValidator implements ConstraintValidator<StringValidation, String> {

    

    private String minLength;
    private String maxLength;

    @Override
    public void initialize(StringValidation stringValidation) {
        minLength  = stringValidation.minLength();
        maxLength =  stringValidation.maxLength();
    }

    public boolean isValid(String input, ConstraintValidatorContext cxt) {
        if (input == null) {
            log.error("input ({}) is null!!!", input);
            return false;
        }
        if (TextUtils.isEmpty(input.trim())) {
            log.error("input ({}) is empty!!!", input);
            return false;
        }
        if (input.length() < Integer.parseInt(minLength)) {
            log.error("input ({}) is less than ({}) char!!!", input, minLength);
            return false;
        }

        if (input.length() > Integer.parseInt(maxLength)) {
            log.error("input ({}) is bigger than ({}) char!!!", input, maxLength);
            return false;
        }
        return true;
    }
}
