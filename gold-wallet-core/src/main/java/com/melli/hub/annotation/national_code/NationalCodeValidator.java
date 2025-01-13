package com.melli.hub.annotation.national_code;

import com.melli.hub.util.PersianUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.TextUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
public class NationalCodeValidator implements ConstraintValidator<NationalCodeValidation, String> {

    private boolean allowEmpty;


    @Override
    public void initialize(NationalCodeValidation nationalCodeValidation) {
        allowEmpty = nationalCodeValidation.allowEmpty();
    }

    public boolean isValid(String nationalCodeInput, ConstraintValidatorContext cxt) {

        String nationalCode = PersianUtils.fromPersianNumeric(nationalCodeInput);

        if (TextUtils.isEmpty(nationalCode) && allowEmpty) {
            return true;
        }


        if (TextUtils.isEmpty(nationalCode) && !allowEmpty) {
            log.error("nationalCode ({}) is empty!!!", nationalCode);
            return false;
        }
        return isValidNationalCode(nationalCode);
    }


    private boolean isValidNationalCode(String nationalCode) {

        if (!StringUtils.hasText(nationalCode)) {
            return false;
        }

        String nationalId = normalNationalCode(nationalCode);
        if (nationalId.length() != 10) {
            return false;
        }
        if (nationalId.equalsIgnoreCase("1111111111") ||
                nationalId.equalsIgnoreCase("2222222222") ||
                nationalId.equalsIgnoreCase("3333333333") ||
                nationalId.equalsIgnoreCase("4444444444") ||
                nationalId.equalsIgnoreCase("5555555555") ||
                nationalId.equalsIgnoreCase("6666666666") ||
                nationalId.equalsIgnoreCase("7777777777") ||
                nationalId.equalsIgnoreCase("8888888888") ||
                nationalId.equalsIgnoreCase("9999999999") ||
                nationalId.equalsIgnoreCase("0000000000")
        ) {
            return false;
        }

        List<String> nationalIdList = new ArrayList<>(Arrays.asList(nationalId.split("")));
        String lastChar = nationalIdList.get(9);
        nationalIdList.remove(9);

        int i = 10;
        int sum = 0;

        for (String value : nationalIdList) {
            sum += Integer.parseInt(value) * i;
            i--;
        }

        int mod = sum % 11;

        if (mod >= 2) {
            mod = 11 - mod;
        }

        return mod == Integer.parseInt(lastChar);

    }

    private String normalNationalCode(String nationalCode) {
        String normalNationalCode = cleanBody(nationalCode);
        return padWithZero(normalNationalCode, 10);
    }

    private String cleanBody(String body) {

        String cleanBody = "";
        if (!StringUtils.hasText(body)) {
            return cleanBody;
        }
        cleanBody = PersianUtils.fromPersianNumeric(body);
        return cleanBody.replaceAll("\\D", "");
    }

    private String padWithZero(String command, int length) {

        String padMessage = command;

        while (padMessage.length() < length) {
            padMessage = "0" + padMessage;
        }
        return padMessage;
    }
}
