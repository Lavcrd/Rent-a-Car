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
    public User(Type type, String name, String surname, LocalDate terminationDate) {
        this.type = type;
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
    @Column(name = "type")
    private Type type;

    public enum Type {
        TYPE_CUSTOMER, TYPE_EMPLOYEE
    }
}
