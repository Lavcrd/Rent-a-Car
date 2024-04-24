package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.global.enums.Role;
import com.sda.rentacar.web.mvc.form.validation.constraint.UserRole;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserRoleValidator implements ConstraintValidator<UserRole, String> {
    private boolean canBeUnselected;

    @Override
    public void initialize(UserRole constraint) {
        this.canBeUnselected = constraint.canBeUnselected();
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            if (input == null) return false;
            if (input.isBlank() && canBeUnselected) return true;
            Role.valueOf(Role.class, input);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}