package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.service.CredentialsService;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.validation.constraint.CurrentPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CurrentPasswordValidator implements ConstraintValidator<CurrentPassword, String> {

    @Autowired
    private CredentialsService credentialsService;

    @Override
    public void initialize(CurrentPassword constraint) {
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return credentialsService.isCurrentPassword(cud, input);
    }
}
