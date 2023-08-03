package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.service.auth.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sda.carrental.model.users.User;
import com.sda.carrental.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final DepartmentService departmentService;
    private final CredentialsService credentialsService;

    public User findById(Long id) {
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
            Optional<User> user = repository.findById(userId);
            if (user.isEmpty()) throw new ResourceNotFoundException();
            User.Roles role = user.get().getRole();

            if (role.equals(User.Roles.ROLE_CUSTOMER)) {
                if (reservationService.hasActiveReservations(userId)) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return HttpStatus.CONFLICT;
                }
                credentialsService.deleteCredentials(user.get().getId());
                return customerService.deleteCustomer(userId, reservationService.getCustomerReservations(userId).isEmpty());
            } else {
                user.get().setTerminationDate(LocalDate.now());
                credentialsService.scramblePassword(userId);
                repository.save(redactUser(user.get()));
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

    public boolean hasNoAccessToUserData(CustomUserDetails cud, Long customerId, Long departmentId) {
        try {
            if (departmentService.departmentAccess(cud, departmentId).equals(HttpStatus.ACCEPTED)) {
                boolean hasReservationsDepTake = reservationService.getUserReservationsByDepartmentTake(customerId, departmentId).isEmpty();
                boolean hasReservationsDepBack = reservationService.getUserReservationsByDepartmentBack(customerId, departmentId).isEmpty();

                return hasReservationsDepTake && hasReservationsDepBack;
            }
            return true;
        } catch (ResourceNotFoundException err) {
            return true;
        }
    }

    public boolean hasNoAccessToUserReservation(CustomUserDetails cud, Long customerId, Long reservationId) {
        try {
            Reservation r = reservationService.getCustomerReservation(customerId, reservationId);

            HttpStatus accessDepFrom = departmentService.departmentAccess(cud, r.getDepartmentTake().getDepartmentId());
            HttpStatus accessDepTo = departmentService.departmentAccess(cud, r.getDepartmentBack().getDepartmentId());

            return accessDepFrom == HttpStatus.FORBIDDEN && accessDepTo == HttpStatus.FORBIDDEN;
        } catch (ResourceNotFoundException err) {
            return true;
        }
    }
}
