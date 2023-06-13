package com.sda.carrental.web.mvc.form;

import com.sda.carrental.model.property.Car;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SubstituteCarFilterForm {
    private Integer priceMin;

    private Integer priceMax;

    private List<String> brands;

    private List<Car.CarType> types;

    private List<Integer> seats;
}
