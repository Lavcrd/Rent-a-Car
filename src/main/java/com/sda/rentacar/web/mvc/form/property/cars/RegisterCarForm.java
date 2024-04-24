package com.sda.rentacar.web.mvc.form.property.cars;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class RegisterCarForm extends ConfirmationForm {

    private Long pattern;

    private Long department;

    @PositiveOrZero(message = "Failure: Price value must be positive.")
    private Long mileage;

    @Pattern(regexp = "\\S{1,10}", message = "Invalid length of license plate.")
    private String plate;

    public RegisterCarForm(Long pattern) {
        this.pattern = pattern;
    }
}
