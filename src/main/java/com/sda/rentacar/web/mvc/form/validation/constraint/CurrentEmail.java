package com.sda.rentacar.web.mvc.form.validation.constraint;

import com.sda.rentacar.web.mvc.form.validation.validator.CurrentEmailValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CurrentEmailValidator.class})
public @interface CurrentEmail {
    String message() default "{com.sda.carrental.web.mvc.form.validation.constraints.CurrentEmail.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
