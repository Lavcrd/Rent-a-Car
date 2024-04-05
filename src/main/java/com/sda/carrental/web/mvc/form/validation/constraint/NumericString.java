package com.sda.carrental.web.mvc.form.validation.constraint;

import com.sda.carrental.web.mvc.form.validation.validator.NumericStringValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {NumericStringValidator.class})
public @interface NumericString {
    boolean allowedPositive() default true;
    boolean allowedZero() default true;
    boolean allowedNegative() default true;
    boolean allowedDouble() default true;
    double min() default -Double.MAX_VALUE;
    double max() default Double.MAX_VALUE;

    String message() default "Failure: Value is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
