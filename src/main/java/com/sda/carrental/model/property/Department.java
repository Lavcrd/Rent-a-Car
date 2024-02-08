package com.sda.carrental.model.property;

import javax.persistence.*;

import com.sda.carrental.global.enums.Country;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "department")
@Getter
@NoArgsConstructor
@ToString
public class Department {

    public Department(Country country, String city, String address, String postcode, String email, String contact, boolean hq) {
        this.country = country;
        this.city = city;
        this.address = address;
        this.postcode = postcode;
        this.email= email;
        this.contact = contact;
        this.hq = hq;
        this.isActive = true;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "country", nullable = false)
    private Country country;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "postcode", nullable = false)
    private String postcode;

    @Setter
    @Column(name = "email", nullable = false)
    private String email;

    @Setter
    @Column(name = "contact", nullable = false)
    private String contact;

    @Setter
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Setter
    @Column(name = "hq", nullable = false)
    private boolean hq;
}
