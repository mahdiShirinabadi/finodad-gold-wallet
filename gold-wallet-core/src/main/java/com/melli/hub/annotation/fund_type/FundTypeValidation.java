package com.melli.hub.annotation.fund_type;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Target( { FIELD, PARAMETER, TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = FundTypeValidator.class)
public @interface FundTypeValidation {

    //error message
    String message() default "مقدار مجاز برای فیلد ({label}) عبارتند از [ETF,NonETF]";

    String label() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
