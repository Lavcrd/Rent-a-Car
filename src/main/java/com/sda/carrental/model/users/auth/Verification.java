package com.sda.carrental.model.users.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "verification")
@Getter
@Setter
@NoArgsConstructor
public class Verification {

    public Verification(Long customerId, String personalId, String driverId) {  //TODO should be encrypted
        this.customerId = customerId;
        this.personalId = personalId;
        this.driverId = driverId;
    }

    @Id
    @Column(name = "customer_id", unique = true)
    private Long customerId;

    @Column(name = "personal_id", unique = true)
    private String personalId;

    @Column(name = "driver_id", unique = true)
    private String driverId;
}