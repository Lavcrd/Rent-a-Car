package com.sda.carrental.web.mvc.form.users;

import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
@RequiredArgsConstructor
public class VerificationForm {
    @ValidCountry(canBeUnselected = false)
    private String country;

    @Pattern(regexp = "\\S{8,16}", message = "Personal IDN must contain minimum 8 characters.")
    private String personalId;

    @Pattern(regexp = "\\S{4,16}", message = "Driver ID must contain minimum 4 characters.")
    private String driverId;
}
