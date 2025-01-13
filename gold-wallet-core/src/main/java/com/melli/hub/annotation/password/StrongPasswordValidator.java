package com.melli.hub.annotation.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.TextUtils;

@Log4j2
public class StrongPasswordValidator implements ConstraintValidator<StrongPasswordValidation, String> {

    private boolean containLowerCase;
    private boolean containUpperCase;
    private boolean containNOSpace;
    private boolean containOneDigit;
    private boolean containSpecialCharacter;
    private int minLength;
    private int maxLength;


    @Override
    public void initialize(StrongPasswordValidation strongPasswordValidation) {
        containLowerCase = strongPasswordValidation.containLowerCase();
        containUpperCase = strongPasswordValidation.containUpperCase();
        containNOSpace = strongPasswordValidation.containNOSpace();
        containOneDigit = strongPasswordValidation.containOneDigit();
        containSpecialCharacter = strongPasswordValidation.containSpecialCharacter();
        minLength  = strongPasswordValidation.minLength();
        maxLength  = strongPasswordValidation.maxLength();
    }

    public boolean isValid(String password, ConstraintValidatorContext cxt) {

        if(TextUtils.isEmpty(password)){
            log.error("password ({}) is empty!!!", password);
            return false;
        }
        return isStrongPassword(password);
    }


    private boolean isStrongPassword(String password) {

        StringBuilder patternRegx= new StringBuilder();
        patternRegx.append("^");

        if (org.apache.commons.lang.StringUtils.isBlank(password)) {
            return false;
        }

        patternRegx.append("^");
        if(containLowerCase){
            patternRegx.append("(?=.*[a-z])");
        }
        if(containUpperCase){
            patternRegx.append("(?=.*[A-Z])");
        }
        if(containOneDigit){
            patternRegx.append("(?=.*[0-9])");
        }
        if(containSpecialCharacter){
            patternRegx.append("(?=.*[@#$%^&+=])");
        }
        if(containNOSpace){
            patternRegx.append("(?=\\S+$)");
        }
        patternRegx.append(".{").append(minLength).append(",").append(maxLength).append("}").append("$");

        return password.matches(patternRegx.toString());
    }

    public static void main(String[] args) {
        boolean allowLowerCase=true;
        boolean allowUpperCase=true;
        boolean allowNOSpace=true;
        boolean allowOneDigit=true;
        boolean allowSpecialCharacter=true;
        int minLength=8;
        int maxLength=20;

        String password = "M@HDi123";

        StringBuilder patternRegx= new StringBuilder();
        patternRegx.append("^");

        if(allowLowerCase){
            patternRegx.append("(?=.*[a-z])");
        }
        if(allowUpperCase){
            patternRegx.append("(?=.*[A-Z])");
        }
        if(allowOneDigit){
            patternRegx.append("(?=.*[0-9])");
        }
        if(allowSpecialCharacter){
            patternRegx.append("(?=.*[@#$%^&+=])");
        }
        if(allowNOSpace){
            patternRegx.append("(?=\\S+$)");
        }
        patternRegx.append(".{").append(minLength).append(",").append(maxLength).append("}").append("$");

        System.out.println(patternRegx.toString());

        System.out.println(password.matches(patternRegx.toString()));
    }
}
