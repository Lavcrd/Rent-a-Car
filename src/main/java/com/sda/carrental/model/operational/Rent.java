package com.sda.carrental.model.operational;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "rent")
@Getter
@NoArgsConstructor
public class Rent {
    public Rent(Long reservationId, Reservation reservation, Long employeeId, String remarks, LocalDate dateFrom, Long mileage) {
        this.id = reservationId;
        this.reservation = reservation;
        this.employeeId = employeeId;
        this.dateFrom = dateFrom;
        this.remarks = remarks;
        this.mileage = mileage;
    }

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @OneToOne
    @JoinColumn(name = "reservation", referencedColumnName = "id")
    private Reservation reservation;

    @Column(name = "actual_date_from")
    private LocalDate dateFrom;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "mileage")
    private Long mileage;
}
