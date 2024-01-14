package com.sda.carrental.service.auth;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import com.sda.carrental.model.users.Admin;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.model.users.auth.Credentials;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sda.carrental.model.users.User;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final Credentials credentials;
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user instanceof Customer u) {
            return Collections.singletonList(new SimpleGrantedAuthority(u.getRole().name()));
        } else if (user instanceof Employee u) {
            return Collections.singletonList(new SimpleGrantedAuthority(u.getRole().name()));
        } else if (user instanceof Admin u) {
            return Collections.singletonList(new SimpleGrantedAuthority(u.getRole().name()));
        }
        return Collections.emptyList();
    }

    public Long getId() {
        return user.getId();
    }

    public User.Type getType() {
        return user.getType();
    }

    @Override
    public String getPassword() {
        return credentials.getPassword();
    }

    @Override
    public String getUsername() {
        return credentials.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getTerminationDate().isAfter(LocalDate.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
