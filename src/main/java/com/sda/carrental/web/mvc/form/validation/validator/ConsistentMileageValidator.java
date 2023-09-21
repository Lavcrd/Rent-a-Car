package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.service.ReservationService;
import com.sda.carrental.web.mvc.form.ConfirmClaimForm;
import com.sda.carrental.web.mvc.form.validation.constraint.ConsistentMileage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConsistentMileageValidator implements ConstraintValidator<ConsistentMileage, ConfirmClaimForm> {

    @Autowired
    private ReservationService rs;

    @Override
    public void initialize(ConsistentMileage constraint) {
    }

    @Override
    public boolean isValid(ConfirmClaimForm form, ConstraintValidatorContext cvc) {
        return rs.findById(form.getReservationId()).getCar().getMileage() < form.getMileage();
    }
}
