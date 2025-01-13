package com.melli.hub.annotation.mobile;

import com.melli.hub.util.PersianUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.util.TextUtils;

import java.util.Arrays;
import java.util.List;

@Log4j2
public class MobileValidator implements ConstraintValidator<MobileValidation, String> {

    private boolean allowEmpty;
    private boolean allowMultiMobile;
    

    @Override
    public void initialize(MobileValidation mobileValidation) {
        allowEmpty  = mobileValidation.allowEmpty();
        allowMultiMobile = mobileValidation.allowMultiMobile();
    }

    public boolean isValid(String mobileNumberInput, ConstraintValidatorContext cxt) {

        String mobileNumber = PersianUtils.fromPersianNumeric(mobileNumberInput);
        String phonePattern = "^(9|09)\\d{9}$";

        if(allowMultiMobile){
            log.info("allow multi mobile is true and start check");
            return isValidMultiMobile(mobileNumberInput, phonePattern);
        }

        if(TextUtils.isEmpty(mobileNumber) && allowEmpty){
            return true;
        }


        if(TextUtils.isEmpty(mobileNumber) && !allowEmpty){
            log.error("mobileNumber ({}) is empty!!!", mobileNumber);
            return false;
        }

        if(mobileNumber.matches(phonePattern)){
            return true;
        }



        log.error("mobileNumber ({}) is not match with regx ({})", mobileNumber, phonePattern);
        return false;
    }


    public boolean isValidMultiMobile(String mobileNumberInput, String phonePattern){
        if(TextUtils.isEmpty(mobileNumberInput)){
            return true;
        }
        List<String> mobileNumberList = Arrays.stream(mobileNumberInput.split(",")).toList();
        if(CollectionUtils.isEmpty(mobileNumberList)){
            log.error("there is no comma (,) exist in string list");
            return false;
        }

        for(String mobile: mobileNumberList){
            String mobileNumber = PersianUtils.fromPersianNumeric(mobile);
            if(!mobileNumber.matches(phonePattern)){
                log.error("mobileNumber ({}) is not a valid format", mobile);
                return false;
            }
        }
        return true;
    }
}
