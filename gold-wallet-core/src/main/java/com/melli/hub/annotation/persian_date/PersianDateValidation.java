package com.melli.hub.annotation.persian_date;

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
@Constraint(validatedBy = PersianDateValidator.class)
public @interface PersianDateValidation {

    //error message
    String message() default "فرمت تاریخ وارد شده برای فیلد ({label})  معتبر نیست، فرمت معتبر: {format} ";

    String label() default "";

    String format() default "yyyy/MM/dd";

    boolean biggerToday() default false;

    //represents group of constraints
    Class<?>[] groups() default {};

    //represents additional information about annotation
    Class<? extends Payload>[] payload() default {};
}
