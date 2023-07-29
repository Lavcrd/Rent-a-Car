package com.sda.carrental.model.users.auth;

import com.sda.carrental.global.enums.Country;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "verification")
@Getter
@Setter
@NoArgsConstructor
public class Verification {

    public Verification(Long customerId, Country country, String personalId, String driverId) {  //TODO should be encrypted
        this.customerId = customerId;
        this.country = country;
        this.personalId = personalId;
        this.driverId = driverId;
    }

    @Id
    @Column(name = "customer_id", unique = true)
    private Long customerId;

    @Column(name = "personal_id", nullable = false)
    private String personalId;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "country", nullable = false)
    private Country country;
}
