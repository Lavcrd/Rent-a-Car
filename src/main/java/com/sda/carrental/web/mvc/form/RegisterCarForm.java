package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.CurrentPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class RegisterCarForm {

    private Long pattern;

    private Long department;

    @PositiveOrZero(message = "Failure: Price value must be positive.")
    private Long mileage;

    @Pattern(regexp = "\\S{1,10}", message = "Invalid length of license plate.")
    private String plate;

    @NotEmpty(message = "Failure: Field cannot be empty")
    @CurrentPassword(message = "Failure: Provided password confirmation is not valid")
    private String currentPassword;
}
