package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidCountryValidator implements ConstraintValidator<ValidCountry, String> {
    private boolean canBeUnselected;

    @Override
    public void initialize(ValidCountry constraint) {
        this.canBeUnselected = constraint.canBeUnselected();
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            if (input == null) return false;
            if (Country.valueOf(Country.class, input).equals(Country.COUNTRY_NONE)) {
                return canBeUnselected;
            }
            return true;
        } catch (RuntimeException err) {
            return false;
        }
    }
}
