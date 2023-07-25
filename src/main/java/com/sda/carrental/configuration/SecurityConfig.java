package com.sda.carrental.configuration;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.sda.carrental.model.users.User;


@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;

    public SecurityConfig(@Qualifier("customUserDetailsService") UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers(HttpMethod.DELETE).hasAnyAuthority(User.Roles.ROLE_EMPLOYEE.name(), User.Roles.ROLE_MANAGER.name(), User.Roles.ROLE_COORDINATOR.name(), User.Roles.ROLE_ADMIN.name())
                .antMatchers(HttpMethod.POST, "/reservation/confirm").authenticated()
                .antMatchers( "/profile", "/profile/password").authenticated()
                .antMatchers( "/profile/**", "/reservations/**").hasAnyAuthority(User.Roles.ROLE_CUSTOMER.name())
                .antMatchers("/mg-cus/**").hasAnyAuthority(User.Roles.ROLE_EMPLOYEE.name(), User.Roles.ROLE_MANAGER.name(), User.Roles.ROLE_COORDINATOR.name(), User.Roles.ROLE_ADMIN.name())
                .antMatchers("/mg-res/**").hasAnyAuthority(User.Roles.ROLE_EMPLOYEE.name(), User.Roles.ROLE_MANAGER.name(), User.Roles.ROLE_COORDINATOR.name(), User.Roles.ROLE_ADMIN.name())
                .antMatchers("/mg-ren/**").hasAnyAuthority(User.Roles.ROLE_EMPLOYEE.name(), User.Roles.ROLE_MANAGER.name(), User.Roles.ROLE_COORDINATOR.name(), User.Roles.ROLE_ADMIN.name())
                .antMatchers("/loc-res/**").hasAnyAuthority(User.Roles.ROLE_EMPLOYEE.name(), User.Roles.ROLE_MANAGER.name(), User.Roles.ROLE_COORDINATOR.name(), User.Roles.ROLE_ADMIN.name())
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .and()
                .httpBasic()
                .and()
                .logout()
                .logoutSuccessUrl("/")
                .and()
                .csrf().disable();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
