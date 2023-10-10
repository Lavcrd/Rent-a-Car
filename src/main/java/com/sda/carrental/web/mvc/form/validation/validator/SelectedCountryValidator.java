package com.sda.carrental.web.mvc.form.validation.validator;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.web.mvc.form.validation.constraint.SelectedCountry;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SelectedCountryValidator implements ConstraintValidator<SelectedCountry, String> {
    @Override
    public void initialize(SelectedCountry constraint) {
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cvc) {
        try {
            return input != null && !Country.valueOf(Country.class, input).equals(Country.COUNTRY_NONE);
        } catch (RuntimeException err) {
            return false;
        }
    }
}
