package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@RequiredArgsConstructor
public class VerificationForm {

    @NonNull
    @NotNull
    private Long customerId;

    @ValidCountry(canBeUnselected = false)
    private String country;

    @Pattern(regexp = "\\S{8,16}", message = "Personal IDN must contain minimum 8 characters.")
    private String personalId;

    @Pattern(regexp = "\\S{4,16}", message = "Driver ID must contain minimum 4 characters.")
    private String driverId;
}
