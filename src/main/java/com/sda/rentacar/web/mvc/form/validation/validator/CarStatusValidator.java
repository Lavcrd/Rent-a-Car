package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.model.property.car.Car;
import com.sda.rentacar.web.mvc.form.validation.constraint.CarStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CarStatusValidator implements ConstraintValidator<CarStatus, String> {
    private boolean canBeEmpty;

    @Override
    public void initialize(CarStatus constraint) {
        this.canBeEmpty = constraint.canBeEmpty();
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            if (input == null) return false;
            if (canBeEmpty && input.isEmpty()) return true;
            Car.CarStatus.valueOf(Car.CarStatus.class, input);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}