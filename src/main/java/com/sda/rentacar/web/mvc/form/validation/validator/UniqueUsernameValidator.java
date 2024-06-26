package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.service.CredentialsService;
import com.sda.rentacar.web.mvc.form.validation.constraint.UniqueUsername;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    private CredentialsService credentialsService;

    @Override
    public void initialize(UniqueUsername constraint) {
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            return credentialsService.isUsernameUnique(input);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
