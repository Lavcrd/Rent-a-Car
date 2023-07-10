package com.sda.carrental.model.operational;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "rent")
@Getter
@NoArgsConstructor
public class Renting {
    public Renting(Long employeeId, Long reservationId, String remarks) {
        this.employeeId = employeeId;
        this.reservationId = reservationId;
        this.actualDateFrom = LocalDate.now();
        this.remarks = remarks;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rent_id", nullable = false)
    private Long rentId;

    @Column(name = "employee_id")
    private Long employeeId;

    @JoinColumn(name = "reservation_id", unique = true)
    private Long reservationId;

    @Column(name = "actual_date_from")
    private LocalDate actualDateFrom;

    @Column(name = "notes")
    private String remarks;
}
