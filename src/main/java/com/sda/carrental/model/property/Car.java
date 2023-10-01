package com.sda.carrental.model.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "car")
@Getter
@NoArgsConstructor
public class Car {
    public Car(Department department, String jpgLink, String brand, String model, Integer year, String plate, Long mileage, Integer seats, Double priceDay, CarType carType, CarStatus carStatus, Double depositValue) {
        this.plate = plate;
        this.department = department;
        this.jpgLink = jpgLink;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.seats = seats;
        this.priceDay = priceDay;
        this.carType = carType;
        this.carStatus = carStatus;
        this.depositValue = depositValue;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "department", referencedColumnName = "id")
    Department department;

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
    @Column(name = "plate", unique = true)
    String plate;

    @Setter
    @Column(name = "mileage")
    Long mileage;

    @Column(name = "seats")
    Integer seats;

    @Column(name = "price_day")
    Double priceDay;

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
