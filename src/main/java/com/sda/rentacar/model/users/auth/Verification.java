package com.sda.rentacar.model.users.auth;

import com.sda.rentacar.model.property.department.Country;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "verification")
@Getter
@Setter
@NoArgsConstructor
public class Verification {

    public Verification(Long customerId, Country country, String personalId, String driverId) {
        this.id = customerId;
        this.country = country;
        this.personalId = personalId;
        this.driverId = driverId;
    }

    @Id
    @Column(name = "id", unique = true)
    private Long id;

    @Column(name = "personal_id", nullable = false)
    private String personalId;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @ManyToOne
    @JoinColumn(name = "country", referencedColumnName = "id")
    private Country country;
}
