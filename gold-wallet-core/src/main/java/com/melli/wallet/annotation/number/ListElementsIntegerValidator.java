package com.melli.wallet.annotation.number;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.util.List;

@Log4j2
public class ListElementsIntegerValidator implements ConstraintValidator<ListElementsInteger, List<?>> {

    private static final String NUMBER_PATTERN = "^\\d+$";
    private static final String NEGATIVE_NUMBER_PATTERN = "^-?\\d+$";

    private boolean allowEmpty;
    private boolean allowNegative;
    private String min;
    private String max;
    private String label;

    @Override
    public void initialize(ListElementsInteger constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.allowNegative = constraintAnnotation.allowNegative();
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.label = constraintAnnotation.label();
    }

    @Override
    public boolean isValid(List<?> list, ConstraintValidatorContext context) {
        // Check if the list is null or empty
        if (list == null || list.isEmpty()) {
            if (allowEmpty) {
                return true;
            }
            log.error("List ({}) is null or empty but allowEmpty is false", label);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("لیست (%s) نمی‌تواند خالی باشد", label)
            ).addConstraintViolation();
            return false;
        }

        String numberPattern = allowNegative ? NEGATIVE_NUMBER_PATTERN : NUMBER_PATTERN;

        // Validate each element in the list
        for (Object item : list) {
            String value;
            if (item instanceof Integer) {
                value = item.toString();
            } else if (item instanceof String) {
                value = (String) item;
                if (!StringUtils.hasText(value)) {
                    log.error("Element in list ({}) is an empty string", label);
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        String.format("فیلد در لیست (%s) نمی‌تواند خالی باشد", label)
                    ).addConstraintViolation();
                    return false;
                }
            } else {
                log.error("Element in list ({}) is of type {} and cannot be parsed to Integer", label, item.getClass().getSimpleName());
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("فیلد در لیست (%s) باید عدد صحیح یا رشته قابل تبدیل به عدد باشد", label)
                ).addConstraintViolation();
                return false;
            }

            // Validate the number pattern
            if (!value.matches(numberPattern)) {
                log.error("Element ({}) in list ({}) does not match pattern ({})", value, label, numberPattern);
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("فیلد (%s) در لیست (%s) باید عدد صحیح%s باشد", value, label, allowNegative ? " (منفی یا مثبت)" : "")
                ).addConstraintViolation();
                return false;
            }

            // Validate min and max bounds
            try {
                long number = Long.parseLong(value);
                if (StringUtils.hasText(min) && number < Long.parseLong(min)) {
                    log.error("Element ({}) in list ({}) is less than min value ({})", number, label, min);
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        String.format("فیلد (%s) در لیست (%s) نباید از مقدار (%s) کمتر باشد", value, label, min)
                    ).addConstraintViolation();
                    return false;
                }
                if (StringUtils.hasText(max) && number > Long.parseLong(max)) {
                    log.error("Element ({}) in list ({}) is more than max value ({})", number, label, max);
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        String.format("فیلد (%s) در لیست (%s) نباید از مقدار (%s) بیشتر باشد", value, label, max)
                    ).addConstraintViolation();
                    return false;
                }
            } catch (NumberFormatException e) {
                log.error("Element ({}) in list ({}) cannot be parsed to a number: {}", value, label, e.getMessage());
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("فیلد (%s) در لیست (%s) قابل تبدیل به عدد صحیح نیست", value, label)
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}