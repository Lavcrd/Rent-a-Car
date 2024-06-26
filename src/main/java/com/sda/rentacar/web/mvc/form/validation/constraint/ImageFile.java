package com.sda.rentacar.web.mvc.form.validation.constraint;

import com.sda.rentacar.web.mvc.form.validation.validator.ImageFileValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ImageFileValidator.class})
public @interface ImageFile {
    String message() default "Failure: Incorrect file format.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
