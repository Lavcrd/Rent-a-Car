package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.CurrentPassword;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class DepositForm {
    @NotEmpty(message = "Field must contain a value")
    @Pattern(regexp="^\\d+,?\\d{0,2}$", message="Not allowed characters")
    private String value;

    @NotEmpty(message = "Field cannot be empty")
    @CurrentPassword(message = "Provided password confirmation is not valid")
    private String currentPassword;
}
