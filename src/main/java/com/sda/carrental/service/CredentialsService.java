package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.Encryption;
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
    private final Encryption e;

    public Credentials findById(Long id) throws RuntimeException {
        Credentials credentials = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Credentials", "ID", id));
        return decrypt(credentials);
    }

    public boolean isCurrentPassword(CustomUserDetails cud, String inputPassword) {
        try {
            String storedPassword = repository.getPasswordById(cud.getId()).orElseThrow(() -> new ResourceNotFoundException("Credentials", "ID", cud.getId()));
            return encoder.matches(inputPassword, storedPassword);
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    @Transactional
    public HttpStatus changePassword(Long userId, String inputPassword) {
        try {
            Credentials credentials = findById(userId);
            credentials.setPassword(encoder.encode(inputPassword));
            save(credentials);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException e) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus changeUsername(String input) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Credentials credentials = findById(cud.getId());
            credentials.setUsername(input);
            save(credentials);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException e) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public boolean isUsernameUnique(String input) throws RuntimeException {
        return repository.findByUsername(e.encrypt(input)).isEmpty();
    }

    @Transactional
    public void createCredentials(Long id, String username, String password) throws RuntimeException {
        Credentials credentials = new Credentials(id, username, encoder.encode(password));
        save(credentials);
    }

    @Transactional(rollbackFor = {Exception.class})
    public void deleteCredentials(Long id) {
        repository.findById(id).ifPresent(repository::delete);
    }

    @Transactional
    public void scramblePassword(Long userId) throws RuntimeException {
        Credentials credentials = findById(userId);
        credentials.setPassword(encoder.encode(utility.generateRandomString(30)));
        save(credentials);
    }

    @Transactional
    private void save(Credentials credentials) throws RuntimeException {
        repository.save(encrypt(credentials));
    }

    private Credentials encrypt(Credentials credentials) throws RuntimeException {
        credentials.setUsername(e.encrypt(credentials.getUsername()));
        return credentials;
    }

    private Credentials decrypt(Credentials credentials) throws RuntimeException {
        credentials.setUsername(e.encrypt(credentials.getUsername()));
        return credentials;
    }
}
