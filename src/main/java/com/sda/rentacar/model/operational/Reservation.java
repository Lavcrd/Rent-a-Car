package com.sda.rentacar.model.operational;

import java.time.LocalDate;

import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.property.car.CarBase;
import com.sda.rentacar.model.users.Customer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {
    public Reservation(Customer customer, CarBase carBase, Department departmentTake, Department departmentBack, LocalDate dateFrom, LocalDate dateTo, LocalDate dateCreated) {
        this.customer = customer;
        this.carBase = carBase;
        this.departmentTake = departmentTake;
        this.departmentBack = departmentBack;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.dateCreated = dateCreated;
        this.status = ReservationStatus.STATUS_PENDING;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "car_base", referencedColumnName = "id")
    private CarBase carBase;

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private Department departmentTake;

    @ManyToOne
    @JoinColumn(name = "department_return_id", referencedColumnName = "id")
    private Department departmentBack;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "date_created")
    private LocalDate dateCreated;

    @Column(name = "status")
    private ReservationStatus status;

    @Getter
    public enum ReservationStatus {
        STATUS_COMPLETED("Completed"),
        STATUS_PROGRESS("In progress"),
        STATUS_PENDING("Pending"),
        STATUS_RESERVED("Reserved"),
        STATUS_CANCELED("Cancelled"),
        STATUS_REFUNDED("Refunded");

        final String text;
        ReservationStatus(String text) {
            this.text = text;
        }
    }
}


