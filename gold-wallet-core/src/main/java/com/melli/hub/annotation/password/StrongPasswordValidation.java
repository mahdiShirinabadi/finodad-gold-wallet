package com.melli.hub.annotation.password;

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
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPasswordValidation {

    //error message
    String message() default "رمز عبور برای فیلد ({label}) ساده میباشد";
    String label() default "";
    int minLength() default 8;
    int maxLength() default 20;
    boolean containOneDigit() default false;
    boolean containLowerCase() default false;
    boolean containUpperCase() default false;
    boolean containSpecialCharacter() default false;
    boolean containNOSpace() default false;
    //represents group of constraints
    Class<?>[] groups() default {};
    //represents additional information about annotation
    Class<? extends Payload>[] payload() default {};
}
