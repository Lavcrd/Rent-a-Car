package com.sda.carrental.web.mvc.form;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
public class FindVerifiedForm {
    @ValidCountry(message = "Provided country is not valid")
    private Country country;

    @Pattern(regexp = "\\S{8,16}", message = "Personal IDN must be in range of 8 to 16 characters")
    private String personalId;
}
