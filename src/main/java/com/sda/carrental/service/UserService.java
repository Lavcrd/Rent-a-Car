package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sda.carrental.model.users.User;
import com.sda.carrental.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final CredentialsService credentialsService;

    public User findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
    }

    public void save(User user) {
        repository.save(user);
    }

    private User redactUser(User user) {
        user.setName("—");
        user.setSurname("—");
        return user;
    }

    @Transactional
    public HttpStatus deleteUser(Long userId) {
        try {
            User user = repository.findById(userId).orElseThrow(ResourceNotFoundException::new);
            User.Type type = user.getType();

            if (type.equals(User.Type.TYPE_CUSTOMER)) {
                if (reservationService.hasActiveReservations(userId)) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return HttpStatus.CONFLICT;
                }
                credentialsService.deleteCredentials(userId);
                return customerService.deleteCustomer(userId, reservationService.findCustomerReservations(userId).isEmpty());
            } else {
                user.setTerminationDate(LocalDate.now());
                credentialsService.scramblePassword(userId);
                repository.save(redactUser(user));
                return HttpStatus.OK;
            }
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus changeContact(String inputContact, long userId) {
        try {
            User user = findById(userId);
            user.setContactNumber(inputContact);
            repository.save(user);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (Error err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
