package com.sda.rentacar.web.mvc.form.validation.constraint;

import com.sda.rentacar.web.mvc.form.validation.validator.CarStatusValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CarStatusValidator.class})
public @interface CarStatus {
    boolean canBeEmpty() default false;

    String message() default "Failure: Provided value is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
