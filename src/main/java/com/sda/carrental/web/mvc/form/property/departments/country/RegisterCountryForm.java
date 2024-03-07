package com.sda.carrental.web.mvc.form.property.departments.country;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class RegisterCountryForm extends ConfirmationForm {

    @NotBlank(message = "Failure: Name field cannot be blank.")
    private String name;

    @NotBlank(message = "Failure: ISO field cannot be blank.")
    @Pattern(regexp = "(^\\w{2}$)|(^\\w{2}-\\w{2}$)", message = "Failure: Invalid ISO pattern.")
    private String code;

    @Pattern(regexp = "^[+]\\d{1,3}$", message = "Failure: Invalid contact pattern.")
    private String contact;

    @NotNull(message = "Failure: Currency field cannot be blank.")
    private Long currency;
}
