package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.model.property.car.CarBase;
import com.sda.rentacar.web.mvc.form.validation.constraint.CarType;

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
        } catch (RuntimeException e) {
            return false;
        }
    }
}