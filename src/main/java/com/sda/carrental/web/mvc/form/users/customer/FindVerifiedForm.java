package com.sda.carrental.web.mvc.form.users.customer;

import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
public class FindVerifiedForm {
    @ValidCountry(canBeUnselected = false)
    private String country;

    @Pattern(regexp = "\\S{8,16}", message = "Personal IDN must be in range of 8 to 16 characters")
    private String personalId;
}
