package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.web.mvc.form.validation.constraint.CarType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CarTypeValidator implements ConstraintValidator<CarType, String> {

    @Override
    public void initialize(CarType constraint) {

    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            CarBase.CarType.valueOf(CarBase.CarType.class, input);
            return true;
        } catch (RuntimeException err) {
            return false;
        }
    }
}