package com.sda.carrental.model.users;

import com.sda.carrental.model.property.Department;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "manager")
@Getter
@NoArgsConstructor
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "manager_id"))

public class Manager extends User {
    public Manager(String name, String surname, Department department, LocalDate terminationDate) {
        super(Roles.ROLE_MANAGER, name, surname, terminationDate);
        this.department = department;
    }

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private Department department;
}
