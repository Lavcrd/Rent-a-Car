package com.sda.carrental.model.users;

import com.sda.carrental.model.property.Department;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "employee")
@Getter
@NoArgsConstructor
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "employee_id"))

public class Employee extends User {
    public Employee(String email, String password, String name, String surname, Department department, LocalDate terminationDate) {
        super(email, password, Roles.ROLE_EMPLOYEE, name, surname, terminationDate);
        this.department = department;
    }

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "department_id")
    private Department department;
}
