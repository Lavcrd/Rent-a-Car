package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.web.mvc.form.validation.constraint.PastOrPresentYear;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class PastOrPresentYearValidator implements ConstraintValidator<PastOrPresentYear, Integer> {

    @Override
    public void initialize(PastOrPresentYear constraint) {
    }

    @Override
    public boolean isValid(Integer input, ConstraintValidatorContext cvc) {
        try {
            return input <= LocalDate.now().getYear();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
