package com.sda.carrental.model.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "car")
@Getter
@NoArgsConstructor
public class Car {
    public Car (Long departmentId, String jpgLink, String brand, String model, Integer year, Long mileage, Integer seats, Double price_day, CarType carType, CarStatus carStatus, Double depositValue) {
        this.departmentId = departmentId;
        this.jpgLink = jpgLink;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.seats = seats;
        this.price_day = price_day;
        this.carType = carType;
        this.carStatus = carStatus;
        this.depositValue = depositValue;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id", nullable = false)
    private Long carId;

    @Setter
    @Column(name = "department_id")
    Long departmentId;

    @Setter
    @Column(name = "jpg_link")
    String jpgLink;

    @Column(name = "brand")
    String brand;

    @Column(name = "model")
    String model;

    @Column(name = "year")
    Integer year;

    @Setter
    @Column(name = "mileage")
    Long mileage;

    @Column(name = "seats")
    Integer seats;

    @Column(name = "price_day")
    Double price_day;

    @Column(name = "type")
    CarType carType;

    @Setter
    @Column(name = "status")
    CarStatus carStatus;

    @Setter
    @Column(name = "deposit")
    Double depositValue;


    @Getter
    public enum CarType {
        TYPE_SEDAN("Sedan"), TYPE_SUV("SUV"), TYPE_COMPACT("Compact"), TYPE_WAGON("Kombi"), TYPE_COUPE("Coupe"), TYPE_VAN("Van"), TYPE_HATCHBACK("Hatchback"), TYPE_PICKUP("Pickup"), TYPE_SPORT("Sport");

        final String name;

        CarType(String name) {
            this.name = name;
        }
    }

    public enum CarStatus {
        STATUS_OPEN, STATUS_RENTED, STATUS_UNAVAILABLE
    }
}
