package com.sda.rentacar.model.operational;

import com.sda.rentacar.model.property.department.Department;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "retrieve")
@Getter
@NoArgsConstructor
public class Retrieve {
    public Retrieve(Long reservationId, Rent rent, Long employeeId, LocalDate dateTo, String remarks, Department department, Long mileage) {
        this.id = reservationId;
        this.rent = rent;
        this.employeeId = employeeId;
        this.dateTo = dateTo;
        this.remarks = remarks;
        this.department = department;
        this.mileage = mileage;
    }

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "rent", referencedColumnName = "id")
    private Rent rent;

    @Column(name = "employee_id")
    private Long employeeId;

    @Setter
    @Column(name = "actual_date_end")
    private LocalDate dateTo;

    @Column(name = "remarks")
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private Department department;

    @Column(name = "mileage")
    private Long mileage;
}
