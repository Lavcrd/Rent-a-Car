package com.sda.carrental.web.mvc.form.validation.constraint;

import com.sda.carrental.web.mvc.form.validation.validator.PastOrPresentYearValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PastOrPresentYearValidator.class})
public @interface PastOrPresentYear {
    String message() default "Failure: Value must be past or present.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
