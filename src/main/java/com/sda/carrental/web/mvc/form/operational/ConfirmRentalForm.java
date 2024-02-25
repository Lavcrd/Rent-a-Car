package com.sda.carrental.web.mvc.form.operational;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.validation.constraint.ConsistentMileage;
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
public class ConfirmRentalForm extends ConfirmationForm {
    @NotNull(message = "Failure: Missing form value.")
    private boolean isIgnoredStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @NotNull(message = "Failure: Missing operation value.")
    private Long reservationId;

    @NotNull(message = "Failure: Car field must contain value.")
    private Long carId;

    @NotNull(message = "Failure: Mileage field must contain value.")
    private Long mileage;

    @NotEmpty(message = "Failure: Remarks field must contain a statement or description.")
    private String remarks;

    public ConfirmRentalForm(Long reservationId, LocalDate dateFrom) {
        this.reservationId = reservationId;
        this.dateFrom = dateFrom;
        this.isIgnoredStatus = false;
    }
}
