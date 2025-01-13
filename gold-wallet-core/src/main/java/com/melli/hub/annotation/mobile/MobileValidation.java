package com.melli.hub.annotation.mobile;

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
@Constraint(validatedBy = MobileValidator.class)
public @interface MobileValidation {

    //error message
    String message() default "شماره همراه وارد شده برای فیلد ({label}) معتبر نمباشد، شماره باید با 09 شروع شود و 11 رقم باشد";

    String label() default "";

    boolean allowEmpty() default false;

    boolean allowMultiMobile() default false;

    //represents group of constraints
    Class<?>[] groups() default {};

    //represents additional information about annotation
    Class<? extends Payload>[] payload() default {};
}
