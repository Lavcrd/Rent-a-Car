package com.sda.rentacar.model.property.car;

import com.sda.rentacar.model.property.department.Department;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "car")
@Getter
@NoArgsConstructor
public class Car {
    public Car(CarBase carBase, Department department, String plate, Long mileage, CarStatus carStatus) {
        this.carBase = carBase;
        this.plate = plate;
        this.department = department;
        this.mileage = mileage;
        this.carStatus = carStatus;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "car_base", referencedColumnName = "id")
    CarBase carBase;

    @Setter
    @ManyToOne
    @JoinColumn(name = "department", referencedColumnName = "id")
    Department department;

    @Setter
    @Column(name = "plate", unique = true)
    String plate;

    @Setter
    @Column(name = "mileage")
    Long mileage;

    @Setter
    @Column(name = "status")
    CarStatus carStatus;

    public enum CarStatus {
        STATUS_OPEN, STATUS_RENTED, STATUS_UNAVAILABLE, STATUS_REMOVED
    }
}
