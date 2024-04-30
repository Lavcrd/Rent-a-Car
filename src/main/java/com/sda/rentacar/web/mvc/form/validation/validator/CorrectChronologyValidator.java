package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.web.mvc.form.operational.IndexForm;
import com.sda.rentacar.web.mvc.form.property.departments.RefreshStatisticsForm;
import com.sda.rentacar.web.mvc.form.users.customer.SearchCustomersForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.CorrectChronology;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CorrectChronologyValidator implements ConstraintValidator<CorrectChronology, Object> {

    @Override
    public void initialize(CorrectChronology constraint) {
    }

    @Override
    public boolean isValid(Object form, ConstraintValidatorContext cvc) {
        if (form instanceof IndexForm f) {
            if (f.getDateFrom() == null || f.getDateTo() == null) return false;
            return !f.getDateFrom().isAfter(f.getDateTo());
        } else if (form instanceof SearchCustomersForm f) {
            if (f.getDateFrom() == null || f.getDateTo() == null) return false;
            return !f.getDateFrom().isAfter(f.getDateTo());
        } else if (form instanceof RefreshStatisticsForm f) {
            if (f.getDateFrom() == null || f.getDateTo() == null) return false;
            return !f.getDateFrom().isAfter(f.getDateTo());
        } else {
            return false;
        }
    }
}
