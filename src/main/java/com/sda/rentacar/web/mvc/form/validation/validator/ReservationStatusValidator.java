package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.model.operational.Reservation;
import com.sda.rentacar.web.mvc.form.validation.constraint.ReservationStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReservationStatusValidator implements ConstraintValidator<ReservationStatus, String> {
    private boolean canBeEmpty;

    @Override
    public void initialize(ReservationStatus constraint) {
        this.canBeEmpty = constraint.canBeEmpty();
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            if (input == null) return false;
            if (canBeEmpty && input.isEmpty()) return true;
            Reservation.ReservationStatus.valueOf(Reservation.ReservationStatus.class, input);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}