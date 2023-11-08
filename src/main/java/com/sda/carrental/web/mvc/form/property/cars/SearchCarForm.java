package com.sda.carrental.web.mvc.form.property.cars;

import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SearchCarForm {
    @ValidCountry(canBeUnselected = false)
    private String country;

    @Pattern(regexp = "\\S{1,10}", message = "Invalid length of license plate.")
    private String plate;
}
