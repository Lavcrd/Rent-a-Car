package com.sda.carrental.web.mvc.form;

import com.sda.carrental.model.property.Car;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SubstituteCarFilterForm {
    private Integer priceMin;

    private Integer priceMax;

    private List<String> brands;

    private List<Car.CarType> types;

    private List<Integer> seats;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Long departmentId;

    public SubstituteCarFilterForm(LocalDate dateFrom, LocalDate dateTo, Long departmentId) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.departmentId = departmentId;
    }
}
