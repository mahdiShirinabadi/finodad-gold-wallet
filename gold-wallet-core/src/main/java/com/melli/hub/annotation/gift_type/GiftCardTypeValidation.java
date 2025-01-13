package com.melli.hub.annotation.gift_type;

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
@Constraint(validatedBy = GiftCardTypeValidator.class)
public @interface GiftCardTypeValidation {

    //error message
    String message() default "مقدار مجاز برای فیلد ({label}) عبارتند از [USED,INITIAL]";

    String label() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
