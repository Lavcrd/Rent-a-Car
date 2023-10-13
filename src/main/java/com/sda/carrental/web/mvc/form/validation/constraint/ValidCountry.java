package com.sda.carrental.web.mvc.form.validation.constraint;

import com.sda.carrental.web.mvc.form.validation.validator.ValidCountryValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidCountryValidator.class})
public @interface ValidCountry {
    boolean canBeUnselected() default true;

    String message() default "Failure: Provided country is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
