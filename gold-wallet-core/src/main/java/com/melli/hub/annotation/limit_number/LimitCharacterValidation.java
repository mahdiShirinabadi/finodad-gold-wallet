package com.melli.hub.annotation.limit_number;

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
@Constraint(validatedBy = LimitCharacterValidator.class)
public @interface LimitCharacterValidation {

    //error message
    String message() default "عبارات مجاز برای فیلد ({label}) به صورت ({allowedValues}) باید باشد";

    String label() default "";

    String allowedValues() default "";

    boolean allowEmpty() default false;

    //represents group of constraints
    Class<?>[] groups() default {};

    //represents additional information about annotation
    Class<? extends Payload>[] payload() default {};
}
