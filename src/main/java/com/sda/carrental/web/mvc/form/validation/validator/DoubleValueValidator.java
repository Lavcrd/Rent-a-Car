package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.web.mvc.form.validation.constraint.DoubleValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DoubleValueValidator implements ConstraintValidator<DoubleValue, String> {
    @Override
    public void initialize(DoubleValue constraint) {
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            if (input == null) return false;
            Double.parseDouble(input);
            return true;
        } catch (RuntimeException err) {
            return false;
        }
    }
}