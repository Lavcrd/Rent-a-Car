package com.sda.carrental.web.mvc.form.property.cars;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class UpdateCarBasePricesForm extends ConfirmationForm {

    @Positive(message = "Failure: Price value must be positive.")
    private Double price;

    @Positive(message = "Failure: Deposit value must be positive.")
    private Double deposit;
}