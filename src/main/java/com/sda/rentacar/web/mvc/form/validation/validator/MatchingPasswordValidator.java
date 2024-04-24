package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.web.mvc.form.users.ChangePasswordForm;
import com.sda.rentacar.web.mvc.form.users.customer.RegisterCustomerForm;
import com.sda.rentacar.web.mvc.form.users.employee.RegisterEmployeeForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.MatchingPassword;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MatchingPasswordValidator implements ConstraintValidator<MatchingPassword, Object> {

    @Override
    public void initialize(MatchingPassword constraint) {
    }

    @Override
    public boolean isValid(Object form, ConstraintValidatorContext cvc) {
        if (form instanceof ChangePasswordForm f) {
            return f.getNewPassword().equals(f.getNewPasswordRepeat());
        } else if (form instanceof RegisterCustomerForm f) {
            return f.getPassword().equals(f.getConfirmPassword());
        } else if (form instanceof RegisterEmployeeForm f) {
            return f.getEmployeePassword().equals(f.getEmployeePasswordRe());
        } else {
            return false;
        }
    }
}
