package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.web.mvc.form.ChangeEmailForm;
import com.sda.carrental.web.mvc.form.validation.constraint.MatchingEmail;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MatchingEmailValidator implements ConstraintValidator<MatchingEmail, ChangeEmailForm> {

    @Override
    public void initialize(MatchingEmail constraint) {
    }

    @Override
    public boolean isValid(ChangeEmailForm cif, ConstraintValidatorContext cvc) {
        return cif.getNewEmail().equals(cif.getNewEmailRepeat());
    }
}
