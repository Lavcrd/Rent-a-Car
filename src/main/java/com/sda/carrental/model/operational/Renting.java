package com.sda.carrental.model.operational;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "rent")
@Getter
@NoArgsConstructor
public class Renting {
    public Renting(Long employeeId, Reservation reservation) {
        this.employeeId = employeeId;
        this.reservation = reservation;
        this.actualDateFrom = LocalDate.now();
        this.remarks = "";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rent_id", nullable = false)
    private Long rentId;

    @Column(name = "employee_id")
    private Long employeeId;

    @OneToOne
    @JoinColumn(name = "reservation_id", referencedColumnName = "reservation_id")
    private Reservation reservation;

    @Column(name = "actual_date_from")
    private LocalDate actualDateFrom;

    @Column(name = "notes")
    private String remarks;
}
