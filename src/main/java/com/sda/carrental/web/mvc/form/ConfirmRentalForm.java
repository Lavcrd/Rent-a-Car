package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.ConsistentMileage;
import com.sda.carrental.web.mvc.form.validation.constraint.CurrentPassword;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ConsistentMileage(message = "Failure: Inconsistent mileage")
public class ConfirmRentalForm {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @NotNull
    private Long reservationId;

    @NotNull(message = "Failure: Field must contain value")
    private Long mileage;

    @NotEmpty(message = "Field must contain a statement or description")
    private String remarks;

    @NotEmpty(message = "Field cannot be empty")
    @CurrentPassword(message = "Provided password confirmation is not valid")
    private String currentPassword;

    public ConfirmRentalForm(Long reservationId, LocalDate dateFrom) {
        this.reservationId = reservationId;
        this.dateFrom = dateFrom;
    }
}
