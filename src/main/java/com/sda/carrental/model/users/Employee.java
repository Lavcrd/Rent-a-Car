package com.sda.carrental.model.users;

import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.property.Department;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "employee_id"))
public class Employee extends User {
    public Employee(String name, String surname, String personalId, List<Department> departments, LocalDate terminationDate, String contactNumber) {
        super(Type.TYPE_EMPLOYEE, name, surname, contactNumber, terminationDate);
        this.personalId = personalId;
        this.role = Role.ROLE_EMPLOYEE;
        this.departments = departments;
    }

    @Column(name = "personal_id")
    private String personalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @ManyToMany(fetch = FetchType.EAGER)
    @Column(name = "departments")
    private List<Department> departments;
}
