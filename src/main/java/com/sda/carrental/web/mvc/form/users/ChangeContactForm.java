package com.sda.carrental.web.mvc.form.users;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class ChangeContactForm extends ConfirmationForm {
    @NotBlank(message = "Field cannot be empty")
    @Pattern(regexp = "^\\+{0,1}[\\s\\d]{6,30}+$", message="Incorrect contact number format")
    private String contactNumber;
}
