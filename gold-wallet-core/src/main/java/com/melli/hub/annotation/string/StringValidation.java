package com.melli.hub.annotation.string;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

@Target( { FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = StringValidator.class)
public @interface StringValidation {

    //error message
    String message() default "تعداد کاراکتر وارد شده در {label} نباید از {minLength} کمتر و از {maxLength} بیشتر باشد";
    String label() default "";
    String minLength() default "2";
    String maxLength() default "1000";
    //represents group of constraints
    Class<?>[] groups() default {};
    //represents additional information about annotation
    Class<? extends Payload>[] payload() default {};
}
