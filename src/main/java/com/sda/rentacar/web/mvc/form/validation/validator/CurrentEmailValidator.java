package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.validation.constraint.CurrentEmail;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CurrentEmailValidator implements ConstraintValidator<CurrentEmail, String> {
    @Override
    public void initialize(CurrentEmail constraint) {
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return cud.getUsername().equals(input);
    }
}
