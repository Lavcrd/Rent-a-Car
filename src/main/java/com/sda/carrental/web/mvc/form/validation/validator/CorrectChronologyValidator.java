package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.web.mvc.form.operational.IndexForm;
import com.sda.carrental.web.mvc.form.users.SearchCustomersForm;
import com.sda.carrental.web.mvc.form.validation.constraint.CorrectChronology;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CorrectChronologyValidator implements ConstraintValidator<CorrectChronology, Object> {

    @Override
    public void initialize(CorrectChronology constraint) {
    }

    @Override
    public boolean isValid(Object form, ConstraintValidatorContext cvc) {
        if (form instanceof IndexForm f) {
            return !f.getDateFrom().isAfter(f.getDateTo());
        } else if (form instanceof SearchCustomersForm f) {
            return !f.getDateFrom().isAfter(f.getDateTo());
        } else {
            return false;
        }
    }
}
