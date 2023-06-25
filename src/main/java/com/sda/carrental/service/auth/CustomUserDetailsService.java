package com.sda.carrental.service.auth;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.auth.Credentials;
import com.sda.carrental.repository.CredentialsRepository;
import com.sda.carrental.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        Credentials credentials = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credentials of " + username + " not found!"));

        return new CustomUserDetails(
                credentials,
                userRepository.findById(credentials.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "ID", credentials.getUserId()))
        );
    }
}
