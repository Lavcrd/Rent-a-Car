package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.service.CarService;
import com.sda.carrental.web.mvc.form.operational.ConfirmClaimForm;
import com.sda.carrental.web.mvc.form.operational.ConfirmRentalForm;
import com.sda.carrental.web.mvc.form.validation.constraint.ConsistentMileage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConsistentMileageValidator implements ConstraintValidator<ConsistentMileage, Object> {

    @Autowired
    private CarService carService;

    @Override
    public void initialize(ConsistentMileage constraint) {
    }

    @Override
    public boolean isValid(Object form, ConstraintValidatorContext cvc) {
        if (form instanceof ConfirmClaimForm ccf) {
            return carService.findByOperationId(ccf.getReservationId()).getMileage() <= ccf.getMileage();
        } else if (form instanceof ConfirmRentalForm crf) {
            return carService.findCarById(crf.getCarId()).getMileage() <= crf.getMileage();
        } else {
            return false;
        }
    }
}
