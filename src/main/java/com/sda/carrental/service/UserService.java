package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.service.auth.CustomUserDetails;
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
    private final DepartmentService departmentService;
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

    public boolean hasNoAccessToUserData(CustomUserDetails cud, Long customerId, Long departmentId) {
        try {
            if (departmentService.departmentAccess(cud, departmentId).equals(HttpStatus.ACCEPTED)) {
                boolean hasNoReservationsDepTake = reservationService.findUserReservationsByDepartmentTake(customerId, departmentId).isEmpty();
                boolean hasNoReservationsDepBack = reservationService.findUserReservationsByDepartmentBack(customerId, departmentId).isEmpty();

                return hasNoReservationsDepTake && hasNoReservationsDepBack;
            }
            return true;
        } catch (ResourceNotFoundException err) {
            return true;
        }
    }

    public boolean hasNoAccessToUserOperation(CustomUserDetails cud, Long customerId, Long operationId) {
        try {
            Reservation r = reservationService.findCustomerReservation(customerId, operationId);

            HttpStatus accessDepFrom = departmentService.departmentAccess(cud, r.getDepartmentTake().getId());
            HttpStatus accessDepTo = departmentService.departmentAccess(cud, r.getDepartmentBack().getId());

            return accessDepFrom == HttpStatus.FORBIDDEN && accessDepTo == HttpStatus.FORBIDDEN;
        } catch (ResourceNotFoundException err) {
            return true;
        }
    }

    public boolean hasNoAccessToProperty(CustomUserDetails cud, Object object) {
        try {
            if (object instanceof Department d) {
                HttpStatus accessDep = departmentService.departmentAccess(cud, d.getId());
                return accessDep == HttpStatus.FORBIDDEN;
            } else if (object instanceof Car c) {
                HttpStatus accessDep = departmentService.departmentAccess(cud, c.getDepartment().getId());
                return accessDep == HttpStatus.FORBIDDEN;
            } else {
                return true;
            }
        } catch (ResourceNotFoundException err) {
            return true;
        }
    }

}
