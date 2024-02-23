package com.sda.carrental.web.mvc.form.validation.constraint;

import com.sda.carrental.web.mvc.form.validation.validator.DoubleValueValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DoubleValueValidator.class})
public @interface DoubleValue {
    boolean allowedPositive() default false;
    boolean allowedZero() default false;
    boolean allowedNegative() default false;

    String message() default "Failure: Value is not valid.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
