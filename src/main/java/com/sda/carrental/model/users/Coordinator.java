package com.sda.carrental.model.users;

import com.sda.carrental.model.property.Department;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity(name = "coordinator")
@Getter
@NoArgsConstructor
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "coordinator_id"))

public class Coordinator extends User {
    public Coordinator(String name, String surname, List<Department> departments, LocalDate terminationDate) {
        super(Roles.ROLE_COORDINATOR, name, surname, terminationDate);
        this.departments = departments;
    }

    @OneToMany
    @Column(name = "departments")
    private List<Department> departments;
}
