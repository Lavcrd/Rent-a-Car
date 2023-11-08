package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.web.mvc.form.users.ChangePasswordForm;
import com.sda.carrental.web.mvc.form.users.RegisterCustomerForm;
import com.sda.carrental.web.mvc.form.validation.constraint.MatchingPassword;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MatchingPasswordValidator implements ConstraintValidator<MatchingPassword, Object> {

    @Override
    public void initialize(MatchingPassword constraint) {
    }

    @Override
    public boolean isValid(Object form, ConstraintValidatorContext cvc) {
        if (form instanceof ChangePasswordForm cif) {
            return cif.getNewPassword().equals(cif.getNewPasswordRepeat());
        } else if (form instanceof RegisterCustomerForm cif) {
            return cif.getPassword().equals(cif.getConfirmPassword());
        } else {
            return false;
        }
    }
}
