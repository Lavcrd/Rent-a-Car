package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.web.mvc.form.validation.constraint.NumericString;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NumericStringValidator implements ConstraintValidator<NumericString, String> {
    private boolean allowedPositive;
    private boolean allowedZero;
    private boolean allowedNegative;
    private boolean allowedDouble;
    private double min;
    private double max;

    @Override
    public void initialize(NumericString constraint) {
        this.allowedPositive = constraint.allowedPositive();
        this.allowedZero = constraint.allowedZero();
        this.allowedNegative = constraint.allowedNegative();
        this.allowedDouble = constraint.allowedDouble();
        this.min = constraint.min();
        this.max = constraint.max();
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            if (allowedDouble) {
                double value = Double.parseDouble(input);
                return check(value);
            } else if (!input.contains(".")) {
                long value = Long.parseLong(input);
                return check(value);
            }
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean check(double value) {
        if (value < min || value > max) return false;
        if (value > 0 && allowedPositive) return true;
        if (value == 0 && allowedZero) return true;
        if (value < 0 && allowedNegative) return true;

        return false;
    }
}