package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.service.auth.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sda.carrental.model.users.User;
import com.sda.carrental.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VerificationService verificationService;
    private final CustomerService customerService;
    private final ReservationService reservationService;


    public User findByUsername(String username) {
        return repository.findByEmail(username).orElseThrow(() -> new RuntimeException("User with username: " + username + " not found"));
    }

    public void save(User user) {
        user.setPassword((bCryptPasswordEncoder.encode(user.getPassword())));
        repository.save(user);
    }

    public boolean isCurrentPassword(CustomUserDetails cud, String inputPassword) {
        try {
            String storedPassword = repository.getPasswordByUsername(cud.getUsername()).orElseThrow(() -> new RuntimeException("User with username: " + cud.getUsername() + " not found"));
            return bCryptPasswordEncoder.matches(inputPassword, storedPassword);
        } catch (RuntimeException err) {
            return false;
        }
    }

    public HttpStatus changePassword(String inputPassword) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = findByUsername(cud.getUsername());
            user.setPassword(bCryptPasswordEncoder.encode(inputPassword));
            repository.save(user);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    public HttpStatus changeEmail(String inputEmail) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = findByUsername(cud.getUsername());
            user.setEmail(inputEmail);
            repository.save(user);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public boolean isEmailUnique(String input) {
        return repository.findByEmail(input).isEmpty();
    }

    private User scrambleUser(User user) {
        user.setName("—");
        user.setSurname("—");
        user.setPassword(customerService.generateRandomString(30));
        return user;
    }

    @Transactional
    public HttpStatus deleteUser(Long userId) {
        try {
            Optional<User> user =  repository.findById(userId);
            if(user.isEmpty()) throw new ResourceNotFoundException();
            User.Roles role = user.get().getRole();

            if (role.equals(User.Roles.ROLE_CUSTOMER)) {
                verificationService.deleteVerification(userId);
                return customerService.deleteCustomer(userId, reservationService.getCustomerReservations(userId).isEmpty());
            } else {
                user.get().setTerminationDate(LocalDate.now());
                repository.save(scrambleUser(user.get()));
                return HttpStatus.OK;
            }
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

    }
}
