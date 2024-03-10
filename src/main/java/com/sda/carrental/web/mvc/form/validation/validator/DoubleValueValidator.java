package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.web.mvc.form.validation.constraint.DoubleValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DoubleValueValidator implements ConstraintValidator<DoubleValue, String> {
    private boolean allowedPositive;
    private boolean allowedZero;
    private boolean allowedNegative;

    @Override
    public void initialize(DoubleValue constraint) {
        this.allowedPositive = constraint.allowedPositive();
        this.allowedZero = constraint.allowedZero();
        this.allowedNegative = constraint.allowedNegative();
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            double value = Double.parseDouble(input);

            if (value > 0 && allowedPositive) return true;
            if (value == 0 && allowedZero) return true;
            if (value < 0 && allowedNegative) return true;

            return false;
        } catch (RuntimeException err) {
            return false;
        }
    }
}