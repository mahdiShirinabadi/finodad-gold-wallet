package com.melli.wallet.annotation.number;

import com.melli.wallet.util.PersianUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NumberValidator implements ConstraintValidator<NumberValidation, String> {

    // Updated patterns
    private static final String DECIMAL_NUMBER_PATTERN = "^[-+]?\\d+(\\.\\d+)?$";
    private static final String INTEGER_NUMBER_PATTERN = "^[-+]?\\d+$";

    private boolean allowEmpty;
    private boolean allowNegative;
    private boolean allowDecimal;

    @Override
    public void initialize(NumberValidation constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.allowNegative = constraintAnnotation.allowNegative();
        this.allowDecimal = constraintAnnotation.allowDecimal();
    }

    @Override
    public boolean isValid(String inputNumber, ConstraintValidatorContext context) {
        // Convert Persian/Arabic numerals to standard numerals
        String number = PersianUtils.fromPersianNumeric(inputNumber);

        // Handle empty case
        if (StringUtils.isEmpty(number)) {
            return allowEmpty;
        }

        // Remove any thousands separators if needed (optional)
        number = number.replace(",", "");

        // Choose appropriate pattern
        String pattern = allowDecimal ? DECIMAL_NUMBER_PATTERN : INTEGER_NUMBER_PATTERN;

        // Validate against pattern
        if (!number.matches(pattern)) {
            log.error("Number '{}' doesn't match required format. Pattern: {}", number, pattern);
            return false;
        }

        // Additional negative check if negative numbers are not allowed
        if (!allowNegative && number.startsWith("-")) {
            log.error("Negative numbers are not allowed for value: {}", number);
            return false;
        }

        return true;
    }
}
