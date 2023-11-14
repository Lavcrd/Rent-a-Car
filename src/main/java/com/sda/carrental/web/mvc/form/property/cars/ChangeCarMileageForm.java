package com.sda.carrental.web.mvc.form.property.cars;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ChangeCarMileageForm extends ConfirmationForm {
    @NotNull(message = "Failure: Value cannot be empty")
    @Min(value = 0, message = "Failure: Provided value must be positive")
    private Long mileage;

    public ChangeCarMileageForm(Long mileage) {
        super();
        this.mileage = mileage;
    }
}