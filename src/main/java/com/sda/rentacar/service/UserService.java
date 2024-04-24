package com.sda.rentacar.service;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.global.Encryption;
import com.sda.rentacar.global.Utility;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sda.rentacar.model.users.User;
import com.sda.rentacar.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.EntityManager;
import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final CredentialsService credentialsService;
    private final EntityManager entityManager;
    private final Encryption e;
    private final Utility u;

    public User findById(Long id) throws RuntimeException {
        User user = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        entityManager.detach(user);
        return decrypt(user);

    }

    private User redactUser(User user) {
        user.setName("—");
        user.setSurname("—");
        user.setContactNumber(u.generateRandomString(10));
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
                save(redactUser(user));
                return HttpStatus.OK;
            }
        } catch (ResourceNotFoundException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus changeContact(String inputContact, long userId) {
        try {
            User user = findById(userId);
            user.setContactNumber(inputContact);
            save(user);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException e) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    private void save(User user) throws RuntimeException {
        repository.save(encrypt(user));
    }

    private User encrypt(User user) throws RuntimeException {
        user.setContactNumber(e.encrypt(user.getContactNumber()));
        return user;
    }

    private User decrypt(User user) throws RuntimeException {
        user.setContactNumber(e.decrypt(user.getContactNumber()));
        return user;
    }


}
