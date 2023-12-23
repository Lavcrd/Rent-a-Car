package com.sda.carrental.web.mvc.form.users;

import com.sda.carrental.web.mvc.form.validation.constraint.CorrectChronology;
import com.sda.carrental.web.mvc.form.validation.constraint.ReservationStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@CorrectChronology(message = "Failure: Invalid date chronology")
public class SearchCustomersForm {
    private Long primaryDepartment;

    private Long secondaryDepartment;

    private String customerName;

    private String customerSurname;

    @NotNull(message = "Failure: Field 'date from' cannot be empty")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @NotNull(message = "Failure: Field 'date to' cannot be empty")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    @ReservationStatus(canBeEmpty = true)
    private String status;

    public SearchCustomersForm() {
        this.dateFrom = LocalDate.now().minusWeeks(1);
        this.dateTo = LocalDate.now().plusWeeks(1);
    }
}
