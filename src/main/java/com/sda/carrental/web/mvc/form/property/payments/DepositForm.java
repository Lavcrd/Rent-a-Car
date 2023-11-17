package com.sda.carrental.web.mvc.form.property.payments;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
public class DepositForm extends ConfirmationForm {
    @NotEmpty(message = "Field must contain a value")
    @Pattern(regexp = "^\\d+,?\\d{0,2}$", message = "Incorrect value format")
    private String value;
}
