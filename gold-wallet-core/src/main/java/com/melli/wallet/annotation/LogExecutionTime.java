package com.melli.wallet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be logged for execution time
 * This annotation can be used on methods to automatically log start and end times
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
    
    /**
     * Optional custom message to include in the log
     * @return custom message or empty string
     */
    String value() default "";
    
    /**
     * Whether to log method parameters
     * @return true if parameters should be logged
     */
    boolean logParameters() default true;
    
    /**
     * Whether to log return value
     * @return true if return value should be logged
     */
    boolean logReturnValue() default true;
}
