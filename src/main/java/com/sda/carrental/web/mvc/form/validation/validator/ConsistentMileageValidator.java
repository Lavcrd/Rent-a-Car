package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.service.CarService;
import com.sda.carrental.web.mvc.form.ConfirmClaimForm;
import com.sda.carrental.web.mvc.form.ConfirmRentalForm;
import com.sda.carrental.web.mvc.form.validation.constraint.ConsistentMileage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConsistentMileageValidator implements ConstraintValidator<ConsistentMileage, Object> {

    @Autowired
    private CarService cs;

    @Override
    public void initialize(ConsistentMileage constraint) {
    }

    @Override
    public boolean isValid(Object form, ConstraintValidatorContext cvc) {
        if (form instanceof ConfirmClaimForm ccf) {
            return cs.findByOperationId(ccf.getReservationId()).getMileage() <= ccf.getMileage();
        } else if (form instanceof ConfirmRentalForm crf) {
            return cs.findCarById(crf.getCarId()).getMileage() <= crf.getMileage();
        } else {
            return false;
        }
    }
}
