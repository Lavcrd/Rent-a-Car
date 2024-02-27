package com.sda.carrental.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "company")
@Getter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "website", nullable = false)
    String website;

    @Column(name = "owner", nullable = false)
    String owner;

    @Column(name = "logotype", nullable = false)
    String logotype;

    public Company(String name, String website, String owner, String logotype) {
        this.name = name;
        this.website = website;
        this.owner = owner;
        this.logotype = logotype;
    }
}

