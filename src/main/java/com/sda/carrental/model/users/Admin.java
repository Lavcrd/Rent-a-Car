package com.sda.carrental.model.users;

import com.sda.carrental.global.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "admin")
@Getter
@NoArgsConstructor
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "admin_id"))

public class Admin extends User {
    public Admin(String name, String surname) {
        super(Type.TYPE_EMPLOYEE, name, surname, LocalDate.ofYearDay(9999, 1));
        this.role = Role.ROLE_ADMIN;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
}
