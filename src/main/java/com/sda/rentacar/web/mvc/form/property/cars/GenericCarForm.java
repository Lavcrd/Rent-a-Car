package com.sda.rentacar.web.mvc.form.property.cars;

import com.sda.rentacar.model.property.car.CarBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class GenericCarForm {
    private Integer priceMin;

    private Integer priceMax;

    private List<String> brands;

    private List<CarBase.CarType> types;

    private List<Integer> seats;
}
