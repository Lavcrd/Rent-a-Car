package com.sda.carrental.model.users.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "credentials")
@Getter
@NoArgsConstructor
public class Credentials {
    public Credentials(Long userId, String username, String password) { //todo encrypted
        this.id = userId;
        this.username = username;
        this.password = password;
    }

    @Id
    @Column(name = "id", unique = true, updatable = false)
    private Long id;

    @Setter
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Setter
    @Column(name = "password", unique = true, nullable = false)
    private String password;
}
