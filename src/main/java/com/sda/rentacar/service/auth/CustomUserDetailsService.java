package com.sda.rentacar.service.auth;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.global.Encryption;
import com.sda.rentacar.model.users.User;
import com.sda.rentacar.model.users.auth.Credentials;
import com.sda.rentacar.repository.CredentialsRepository;
import com.sda.rentacar.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final Encryption e;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        Credentials credentials = credentialsRepository.findByUsername(e.encrypt(username))
                .orElseThrow(() -> new UsernameNotFoundException("Credentials of " + username + " not found!"));

        User user = userRepository.findById(credentials.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", credentials.getId()));

        entityManager.detach(credentials);
        entityManager.detach(user);

        return new CustomUserDetails(decrypt(credentials), decrypt(user));
    }

    private User decrypt(User user) throws RuntimeException {
        user.setContactNumber(e.encrypt(user.getContactNumber()));
        return user;
    }

    private Credentials decrypt(Credentials credentials) throws RuntimeException {
        credentials.setUsername(e.decrypt(credentials.getUsername()));
        return credentials;
    }
}
