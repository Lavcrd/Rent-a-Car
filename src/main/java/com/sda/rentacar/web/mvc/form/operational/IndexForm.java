package com.sda.rentacar.web.mvc.form.operational;

import com.sda.rentacar.web.mvc.form.validation.constraint.CorrectChronology;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@CorrectChronology(message = "Incorrect date order")
public class IndexForm {

    @NotNull(message = "No departure location selected")
    private Long departmentIdFrom;

    @NotNull(message = "No arrival location selected")
    private Long departmentIdTo;

    private boolean differentDepartment;

    @FutureOrPresent(message = "Rental date is out of date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @FutureOrPresent(message = "Return date is out of date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    public IndexForm(LocalDate dateFrom, LocalDate dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.differentDepartment = false;
    }
}
