package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    @Query(value = "FROM reservation WHERE customer.id = :customerId ORDER BY reservationId DESC")
    List<Reservation> findAllByCustomer(@Param("customerId") Long customerId);

    @Query(value = "FROM reservation WHERE customer.id = :customerId AND reservationId = :id")
    Optional<Reservation> findByCustomerAndId(@Param("customerId") Long customerId, @Param("id") Long id);

    @Query(value = "FROM reservation r WHERE r.departmentTake.departmentId = :departmentId AND r.customer.id = :customerId ORDER BY r.reservationId DESC")
    List<Reservation> findAllByCustomerIdAndDepartmentId(@Param("customerId") Long customerId, @Param("departmentId") Long departmentId);

    @Query(value = "FROM reservation r WHERE r.customer.id = :customerId AND r.status IN (1, 3) ")
    List<Reservation> findAllActiveByCustomerId(@Param("customerId") Long customerId);
}
