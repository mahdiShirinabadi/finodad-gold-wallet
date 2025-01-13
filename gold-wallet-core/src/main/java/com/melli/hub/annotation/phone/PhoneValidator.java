package com.melli.hub.annotation.phone;

import com.melli.hub.util.PersianUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.TextUtils;

@Log4j2
public class PhoneValidator implements ConstraintValidator<PhoneValidation, String> {

    private static final String PHONE_PATTERN = "\\d{8}$";
    private boolean allowEmpty;


    @Override
    public void initialize(PhoneValidation phoneValidation) {
        allowEmpty = phoneValidation.allowEmpty();
    }

    public boolean isValid(String phoneInput, ConstraintValidatorContext cxt) {

        String phone = PersianUtils.fromPersianNumeric(phoneInput);

        if (TextUtils.isEmpty(phone)) {
            return allowEmpty;
        }
        return phone.matches(PHONE_PATTERN);
    }
}
