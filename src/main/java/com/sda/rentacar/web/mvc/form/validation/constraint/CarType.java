package com.sda.rentacar.web.mvc.form.validation.constraint;

import com.sda.rentacar.web.mvc.form.validation.validator.CarTypeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CarTypeValidator.class})
public @interface CarType {

    String message() default "Failure: Provided type is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
