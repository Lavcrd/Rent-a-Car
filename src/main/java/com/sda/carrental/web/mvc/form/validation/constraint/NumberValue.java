package com.sda.carrental.web.mvc.form.validation.constraint;

import com.sda.carrental.web.mvc.form.validation.validator.NumberValueValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {NumberValueValidator.class})
public @interface NumberValue {
    boolean allowedPositive() default false;
    boolean allowedZero() default false;
    boolean allowedNegative() default false;
    boolean allowedDouble() default false;
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;

    String message() default "Failure: Value is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
