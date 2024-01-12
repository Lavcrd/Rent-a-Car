package com.sda.carrental.configuration;


import com.sda.carrental.global.enums.Role;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


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
                .antMatchers(HttpMethod.DELETE).hasAnyAuthority(Role.ROLE_EMPLOYEE.name(), Role.ROLE_MANAGER.name(), Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
                .antMatchers(HttpMethod.POST, "/reservation/confirm").authenticated()
                .antMatchers( "/profile", "/profile/password").authenticated()
                .antMatchers( "/profile/**", "/reservations/**").hasAnyAuthority(Role.ROLE_CUSTOMER.name())
                .antMatchers("/mg-cus/**").hasAnyAuthority(Role.ROLE_EMPLOYEE.name(), Role.ROLE_MANAGER.name(), Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
                .antMatchers("/mg-res/**").hasAnyAuthority(Role.ROLE_EMPLOYEE.name(), Role.ROLE_MANAGER.name(), Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
                .antMatchers("/loc-res/**").hasAnyAuthority(Role.ROLE_EMPLOYEE.name(), Role.ROLE_MANAGER.name(), Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
                .antMatchers("/c-ret/**").hasAnyAuthority(Role.ROLE_EMPLOYEE.name(), Role.ROLE_MANAGER.name(), Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
                .antMatchers("/mg-depo/**").hasAnyAuthority(Role.ROLE_MANAGER.name(), Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
                .antMatchers("/mg-car/car-bases/**").hasAnyAuthority(Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
                .antMatchers("/mg-car/**").hasAnyAuthority(Role.ROLE_MANAGER.name(), Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
                .antMatchers("/mg-emp/**").hasAnyAuthority(Role.ROLE_MANAGER.name(), Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
                .antMatchers("/archive/**").hasAnyAuthority(Role.ROLE_MANAGER.name(), Role.ROLE_COORDINATOR.name(), Role.ROLE_ADMIN.name())
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
