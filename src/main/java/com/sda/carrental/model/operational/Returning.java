package com.sda.carrental.model.operational;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "returning")
@Getter
@NoArgsConstructor
public class Returning {
    public Returning(Long reservationId, Reservation reservation, Renting renting, Long employeeId, LocalDate dateTo, String remarks) {
        this.id = reservationId;
        this.reservation = reservation;
        this.renting = renting;
        this.employeeId = employeeId;
        this.dateTo = dateTo;
        this.remarks = remarks;
    }

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reservation", referencedColumnName = "id")
    private Reservation reservation;

    @OneToOne
    @JoinColumn(name = "renting", referencedColumnName = "id")
    private Renting renting;

    @Column(name = "employee_id")
    private Long employeeId;

    @Setter
    @Column(name = "actual_date_end")
    private LocalDate dateTo;

    @Column(name = "remarks")
    private String remarks;
}
