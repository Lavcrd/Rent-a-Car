package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.web.mvc.form.operational.IndexForm;
import com.sda.carrental.web.mvc.form.validation.constraint.CorrectChronology;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CorrectChronologyValidator implements ConstraintValidator<CorrectChronology, IndexForm> {

    @Override
    public void initialize(CorrectChronology constraint) {
    }

    @Override
    public boolean isValid(IndexForm cif, ConstraintValidatorContext cvc) {
        return !cif.getDateFrom().isAfter(cif.getDateTo());
    }
}
