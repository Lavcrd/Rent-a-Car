package com.sda.rentacar.web.mvc.form.property.departments;

import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@ToString
public class UpdateMultiplierForm extends ConfirmationForm {
    @NotNull(message = "Failure: Multiplier field cannot be empty.")
    @Positive(message = "Failure: Multiplier value must be positive.")
    private Double multiplier;

    public UpdateMultiplierForm(Department department) {
        this.multiplier = department.getMultiplier();
    }
}
