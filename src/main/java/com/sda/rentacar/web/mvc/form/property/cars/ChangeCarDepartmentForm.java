package com.sda.rentacar.web.mvc.form.property.cars;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ChangeCarDepartmentForm extends ConfirmationForm {
    @NotNull(message = "Failure: Value cannot be empty")
    @Min(value = 0, message = "Failure: Provided value is not valid")
    private Long departmentId;

    public ChangeCarDepartmentForm(Long departmentId) {
        super();
        this.departmentId = departmentId;
    }
}