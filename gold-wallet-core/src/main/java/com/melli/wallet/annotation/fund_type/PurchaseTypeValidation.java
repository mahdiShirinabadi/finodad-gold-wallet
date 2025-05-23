package com.melli.wallet.annotation.fund_type;

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
@Constraint(validatedBy = PurchaseTypeValidator.class)
public @interface PurchaseTypeValidation {

    //error message
    String message() default "مقدار مجاز برای فیلد ({label}) عبارتند از [BUY,SELL]";

    String label() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
