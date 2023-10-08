package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.CarStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeCarStatus extends ConfirmationForm {
    @CarStatus
    private Object status;
}