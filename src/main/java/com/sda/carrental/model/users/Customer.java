package com.sda.carrental.model.users;

import com.sda.carrental.global.enums.Country;
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

    public Customer(String name, String surname, Country country, String city, CustomerStatus status, String address, String contactNumber)
    {
        super(Roles.ROLE_CUSTOMER, name, surname, LocalDate.ofYearDay(9999, 1));
        this.country = country;
        this.city = city;
        this.status = status;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    @Column(name = "country")
    private Country country;

    @Column(name = "city")
    private String city;

    @Column(name = "status")
    private CustomerStatus status;

    @Column(name = "address") //TODO everything below should be encrypted
    private String address;

    @Column(name = "contact_number")
    private String contactNumber;

    public enum CustomerStatus {
        STATUS_REGISTERED(), STATUS_UNREGISTERED(), STATUS_HIDDEN()
    }
}
