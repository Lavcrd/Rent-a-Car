package com.sda.carrental.model.operational;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "returning")
@Getter
@NoArgsConstructor
public class Returning {
    public Returning(Long reservationId, Long employeeId, LocalDate dateTo, String remarks) {
        this.id = reservationId;
        this.employeeId = employeeId;
        this.dateTo = dateTo;
        this.remarks = remarks;
    }

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "actual_date_end")
    private LocalDate dateTo;

    @Column(name = "remarks")
    private String remarks;
}
