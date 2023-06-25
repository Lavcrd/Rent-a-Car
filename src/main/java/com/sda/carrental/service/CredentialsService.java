package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.Utility;
import com.sda.carrental.model.users.auth.Credentials;
import com.sda.carrental.repository.CredentialsRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CredentialsService {
    private final CredentialsRepository repository;
    private final BCryptPasswordEncoder encoder;
    private final Utility utility;

    public Credentials findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Credentials", "ID", id));
    }

    public boolean isCurrentPassword(CustomUserDetails cud, String inputPassword) {
        try {
            String storedPassword = repository.getPasswordById(cud.getId()).orElseThrow(() -> new ResourceNotFoundException("Credentials", "ID", cud.getId()));
            return encoder.matches(inputPassword, storedPassword);
        } catch (ResourceNotFoundException err) {
            err.printStackTrace();
            return false;
        }
    }

    @Transactional
    public HttpStatus changePassword(String inputPassword) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Credentials credentials = findById(cud.getId());
            credentials.setPassword(encoder.encode(inputPassword));
            repository.save(credentials);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus changeUsername(String input) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Credentials credentials = findById(cud.getId());
            credentials.setUsername(input);
            repository.save(credentials);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public boolean isUsernameUnique(String input) {
        return repository.findByUsername(input).isEmpty();
    }

    @Transactional
    public void createCredentials(Long id, String username, String password) {
        repository.save(new Credentials(id, username, encoder.encode(password)));
    }

    @Transactional
    public void deleteCredentials(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void scramblePassword(Long userId) {
        Credentials credentials = findById(userId);
        credentials.setPassword(utility.generateRandomString(30));
        repository.save(credentials);
    }
}
