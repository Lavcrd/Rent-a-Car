package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.SelectedCountry;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
public class FindVerifiedForm {
    @SelectedCountry
    private String country;

    @Pattern(regexp = "\\S{8,16}", message = "Personal IDN must be in range of 8 to 16 characters")
    private String personalId;
}
