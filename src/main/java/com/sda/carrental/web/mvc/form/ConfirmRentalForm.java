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

    @NotNull(message = "Failure: Missing operation value")
    private Long reservationId;

    @NotNull(message = "Failure: Car field must contain value")
    private Long carId;

    @NotNull(message = "Failure: Mileage field must contain value")
    private Long mileage;

    @NotEmpty(message = "Failure: Remarks field must contain a statement or description")
    private String remarks;

    @NotEmpty(message = "Field cannot be empty")
    @CurrentPassword(message = "Provided password confirmation is not valid")
    private String currentPassword;

    public ConfirmRentalForm(Long reservationId, LocalDate dateFrom) {
        this.reservationId = reservationId;
        this.dateFrom = dateFrom;
    }
}
