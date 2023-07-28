package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidCountryValidator implements ConstraintValidator<ValidCountry, Country> {
    @Override
    public void initialize(ValidCountry constraint) {
    }

    @Override
    public boolean isValid(Country input, ConstraintValidatorContext cvc) {
        try {
            if (input.equals(Country.COUNTRY_NONE)) return false;
            Country.valueOf(Country.class, input.name());
            return true;
        } catch (NullPointerException | IllegalArgumentException err) {
            return false;
        }
    }
}
