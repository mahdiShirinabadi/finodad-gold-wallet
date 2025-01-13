package com.melli.hub.annotation.bool;

import com.melli.hub.util.PersianUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.TextUtils;

@Log4j2
public class BooleanValidator implements ConstraintValidator<BooleanValidation, String> {

    private boolean allowEmpty;

    

    @Override
    public void initialize(BooleanValidation numberValidation) {
        allowEmpty  = numberValidation.allowEmpty();
    }

    public boolean isValid(String inputNumber, ConstraintValidatorContext cxt) {
        String bool = PersianUtils.fromPersianNumeric(inputNumber);
        String booleanPattern = "^([Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee])$";
        if(allowEmpty && TextUtils.isEmpty(bool)){
            return true;
        }

        if(TextUtils.isEmpty(bool)){
            log.error("Boolean ({}) is empty!!!", bool);
            return false;
        }
        if(bool.matches(booleanPattern)){
            return true;
        }
        log.error("Boolean ({}) is not match with regx ({})", bool, booleanPattern);
        return false;
    }
}
