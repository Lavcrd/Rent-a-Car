package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.service.ReservationService;
import com.sda.carrental.web.mvc.form.ConfirmClaimForm;
import com.sda.carrental.web.mvc.form.ConfirmRentalForm;
import com.sda.carrental.web.mvc.form.validation.constraint.ConsistentMileage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConsistentMileageValidator implements ConstraintValidator<ConsistentMileage, Object> {

    @Autowired
    private ReservationService rs;

    @Override
    public void initialize(ConsistentMileage constraint) {
    }

    @Override
    public boolean isValid(Object form, ConstraintValidatorContext cvc) {
        if (form instanceof ConfirmClaimForm ccf) {
            return rs.findById(ccf.getReservationId()).getCar().getMileage() <= ccf.getMileage();
        } else if (form instanceof ConfirmRentalForm crf) {
            return rs.findById(crf.getReservationId()).getCar().getMileage() <= crf.getMileage();
        } else {
            return false;
        }
    }
}
