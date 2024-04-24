package com.sda.rentacar.web.mvc.form.property.cars;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SearchCarForm {
    @NotBlank
    private String country;

    @Pattern(regexp = "\\S{1,10}", message = "Invalid length of license plate.")
    private String plate;
}
