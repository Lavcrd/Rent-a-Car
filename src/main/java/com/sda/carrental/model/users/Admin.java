package com.sda.carrental.model.users;

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
        super(Roles.ROLE_ADMIN, name, surname, LocalDate.ofYearDay(9999, 1));
    }
}
