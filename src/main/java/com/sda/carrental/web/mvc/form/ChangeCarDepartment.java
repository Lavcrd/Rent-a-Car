package com.sda.carrental.web.mvc.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ChangeCarDepartment extends ConfirmationForm {
    @NotNull(message = "Failure: Value cannot be empty")
    @Min(value = 0, message = "Failure: Provided value is not valid")
    private Long departmentId;

    public ChangeCarDepartment(Long departmentId) {
        super();
        this.departmentId = departmentId;
    }
}