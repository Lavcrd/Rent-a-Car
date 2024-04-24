package com.sda.rentacar.model.property.car;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "car_base")
@Getter
@NoArgsConstructor
public class CarBase {
    public CarBase(String image, String brand, String model, Integer year, CarType carType, Integer seats, Double priceDay, Double depositValue) {
        this.image = image;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.carType = carType;
        this.seats = seats;
        this.priceDay = priceDay;
        this.depositValue = depositValue;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(name = "image")
    String image;

    @Column(name = "brand")
    String brand;

    @Column(name = "model")
    String model;

    @Column(name = "year")
    Integer year;

    @Column(name = "type")
    CarType carType;

    @Column(name = "seats")
    Integer seats;

    @Setter
    @Column(name = "price_day")
    Double priceDay;

    @Setter
    @Column(name = "deposit")
    Double depositValue;

    @Getter
    public enum CarType {
        TYPE_SEDAN("Sedan"), TYPE_SUV("SUV"), TYPE_COMPACT("Compact"), TYPE_WAGON("Wagon"), TYPE_COUPE("Coupe"), TYPE_VAN("Van"), TYPE_HATCHBACK("Hatchback"), TYPE_PICKUP("Pickup"), TYPE_SPORT("Sport");

        final String name;

        CarType(String name) {
            this.name = name;
        }
    }
}
