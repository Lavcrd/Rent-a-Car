package com.sda.carrental.model.users;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "user")
@Getter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    public User(Roles role, String name, String surname, LocalDate terminationDate) {
        this.role = role;
        this.name = name;
        this.surname = surname;
        this.creationDate = LocalDate.now();
        this.terminationDate = terminationDate;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @Column(name = "surname")
    private String surname;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Setter
    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Roles role;

    public enum Roles {
        ROLE_CUSTOMER, ROLE_EMPLOYEE, ROLE_MANAGER, ROLE_COORDINATOR, ROLE_ADMIN;

        Roles() {
        }
    }
}
