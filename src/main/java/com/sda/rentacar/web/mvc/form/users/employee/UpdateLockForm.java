package com.sda.rentacar.web.mvc.form.users.employee;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateLockForm extends ConfirmationForm {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Failure: Invalid date value.")
    private LocalDate date;

    public UpdateLockForm(LocalDate date) {
        this.date = date;
    }
}