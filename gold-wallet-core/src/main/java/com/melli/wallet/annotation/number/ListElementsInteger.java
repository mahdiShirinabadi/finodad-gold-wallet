package com.melli.wallet.annotation.number;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ListElementsIntegerValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListElementsInteger {

    String message() default "همه عناصر لیست باید اعداد صحیح باشند یا قابل تبدیل به اعداد صحیح باشند";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean allowEmpty() default true;

    boolean allowNegative() default false;

    String min() default "";

    String max() default "";

    String label() default "";
}