package com.sda.carrental.model.property.department;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "department")
@Getter
@NoArgsConstructor
@ToString
public class Department {

    public Department(Country country, String city, String street, String building, String postcode, String email, String contact, boolean hq) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.building = building;
        this.postcode = postcode;
        this.email= email;
        this.contact = contact;
        this.hq = hq;
        this.isActive = true;
        this.multiplier = 1D;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "country", referencedColumnName = "id")
    private Country country;

    @Setter
    @Column(name = "city", nullable = false)
    private String city;

    @Setter
    @Column(name = "street", nullable = false)
    private String street;

    @Setter
    @Column(name = "building", nullable = false)
    private String building;

    @Setter
    @Column(name = "postcode", nullable = false)
    private String postcode;

    @Setter
    @Column(name = "email", nullable = false)
    private String email;

    @Setter
    @Column(name = "contact", nullable = false)
    private String contact;

    @Setter
    @Column(name = "multiplier", nullable = false)
    private Double multiplier;

    @Setter
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Setter
    @Column(name = "hq", nullable = false)
    private boolean hq;
}
