package com.sda.carrental.web.mvc.form;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
public class ReclaimCarForm {

    @ValidCountry(message = "Provided country is not valid")
    private Country country;

    @Pattern(regexp = "\\S{1,10}", message = "Invalid length of license plate.")
    private String plate;
}
