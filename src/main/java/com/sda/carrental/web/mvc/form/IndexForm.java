package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.CorrectChronology;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@CorrectChronology(message = "Incorrect date order")
public class IndexForm {

    private Long departmentIdFrom;

    private Long departmentIdTo;

    private boolean firstBranchChecked;

    @FutureOrPresent(message = "Rental date is out of date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @FutureOrPresent(message = "Return date is out of date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    private LocalDate dateCreated;

    public IndexForm(LocalDate dateFrom, LocalDate dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }
}
