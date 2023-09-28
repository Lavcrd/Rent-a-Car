package com.sda.carrental.web.mvc.form;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SearchCarsForm {

    private Reservation.ReservationStatus status;

    private Long department;

    @NotNull(message = "Field 'country' cannot be empty")
    private Country country;

    private String plate;

    private Long mileage;

    private Integer priceMin;

    private Integer priceMax;

    private List<String> brands;

    private List<Car.CarType> types;

    private List<Integer> seats;
}
