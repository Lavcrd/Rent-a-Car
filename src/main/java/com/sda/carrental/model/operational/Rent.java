package com.sda.carrental.model.operational;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "rent")
@Getter
@NoArgsConstructor
public class Rent {
    public Rent(Long reservationId, Long employeeId, String remarks, LocalDate dateFrom) {
        this.id = reservationId;
        this.employeeId = employeeId;
        this.dateFrom = dateFrom;
        this.remarks = remarks;
    }

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "actual_date_from")
    private LocalDate dateFrom;

    @Column(name = "remarks")
    private String remarks;
}
