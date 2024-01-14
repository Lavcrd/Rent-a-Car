package com.sda.carrental.model.users;

import com.sda.carrental.global.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "customer")
@Getter
@NoArgsConstructor
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "customer_id"))
public class Customer extends User {
    public Customer(String name, String surname, Status status, String contactNumber) {
        super(Type.TYPE_CUSTOMER, name, surname, contactNumber, LocalDate.ofYearDay(9999, 1));
        this.role = Role.ROLE_CUSTOMER;
        this.status = status;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Setter
    @Column(name = "status")
    private Status status;

    public enum Status {
        STATUS_REGISTERED, STATUS_UNREGISTERED, STATUS_DELETED
    }
}
