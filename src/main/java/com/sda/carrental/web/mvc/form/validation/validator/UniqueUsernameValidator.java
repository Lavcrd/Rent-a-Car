package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.service.CredentialsService;
import com.sda.carrental.web.mvc.form.validation.constraint.UniqueUsername;
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
        return credentialsService.isUsernameUnique(input);
    }
}
