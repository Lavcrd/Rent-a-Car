package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.CarStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangeCarStatus extends ConfirmationForm {
    @CarStatus
    private String status;

    public ChangeCarStatus(String status) {
        super();
        this.status = status;
    }
}