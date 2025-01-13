package com.melli.hub.annotation.error_map;

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
@Constraint(validatedBy = StockErrorMapValidator.class)
public @interface StockErrorMapValidation {

    //error message
    String message() default "مقدار مجاز برای فیلد ({label}) عبارتند از ['CUSTOM_MESSAGE','CHANNEL_MESSAGE','STATUS_TABLE']";

    String label() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
