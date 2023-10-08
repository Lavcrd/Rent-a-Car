package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.model.property.Car;
import com.sda.carrental.web.mvc.form.validation.constraint.CarStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CarStatusValidator implements ConstraintValidator<CarStatus, Object> {
    @Override
    public void initialize(CarStatus constraint) {
    }

    @Override
    public boolean isValid(Object input, ConstraintValidatorContext cvc) {
        try {
            if (input == null) return false;
            Car.CarStatus.valueOf((String) input);
            return true;
        } catch (RuntimeException err) {
            return false;
        }
    }
}