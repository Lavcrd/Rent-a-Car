package com.sda.carrental.model.users;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "customer_id"))
public class Customer extends User
{

    public Customer(String name, String surname, CustomerStatus status, String contactNumber)
    {
        super(Roles.ROLE_CUSTOMER, name, surname, LocalDate.ofYearDay(9999, 1));
        this.status = status;
        this.contactNumber = contactNumber;
    }

    @Column(name = "status")
    private CustomerStatus status;

    @Column(name = "contact_number")
    private String contactNumber;

    public enum CustomerStatus {
        STATUS_REGISTERED(), STATUS_UNREGISTERED(), STATUS_DELETED()
    }
}
