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
        this.rentId= reservationId;
        this.dateFrom = LocalDate.now();
        this.remarks = remarks;
    }

    @Id
    @Column(name = "rent_id", nullable = false, unique = true)
    private Long rentId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "actual_date_from")
    private LocalDate dateFrom;

    @Column(name = "remarks")
    private String remarks;
}
