package com.sda.rentacar.web.mvc.form.property.cars;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.CarStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangeCarStatusForm extends ConfirmationForm {
    @CarStatus
    private String status;

    public ChangeCarStatusForm(String status) {
        super();
        this.status = status;
    }
}