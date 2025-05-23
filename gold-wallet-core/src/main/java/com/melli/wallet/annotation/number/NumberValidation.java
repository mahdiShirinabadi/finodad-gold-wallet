package com.melli.wallet.annotation.number;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

@Target({FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NumberValidator.class)
public @interface NumberValidation {

    //error message
    String message() default "مقدار وارد شده برای فیلد ({label}) به صورت عدد باید باشد";

    String label() default "";

    boolean allowEmpty() default false;

    boolean allowNegative() default false;

    boolean allowDecimal() default false;

    String minDecimalValue() default "0";

    //represents group of constraints
    Class<?>[] groups() default {};

    //represents additional information about annotation
    Class<? extends Payload>[] payload() default {};
}
