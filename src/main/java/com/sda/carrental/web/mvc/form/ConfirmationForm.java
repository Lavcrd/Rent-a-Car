package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.CurrentPassword;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ConfirmationForm {

    @NotEmpty(message = "Field cannot be empty")
    @CurrentPassword(message = "Provided current password is incorrect")
    private String currentPassword;
}
