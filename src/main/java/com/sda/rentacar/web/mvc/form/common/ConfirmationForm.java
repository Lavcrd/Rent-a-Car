package com.sda.rentacar.web.mvc.form.common;

import com.sda.rentacar.web.mvc.form.validation.constraint.CurrentPassword;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ConfirmationForm {

    @NotEmpty(message = "Field cannot be empty")
    @CurrentPassword(message = "Provided password confirmation is not valid")
    private String currentPassword;
}
