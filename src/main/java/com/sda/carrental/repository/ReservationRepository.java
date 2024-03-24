package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    @Query(value = "SELECT r FROM reservation r WHERE r.customer.id = :customerId ORDER BY id DESC")
    List<Reservation> findAllByCustomerId(@Param("customerId") Long customerId);

    @Query(value = "SELECT r FROM reservation r WHERE r.customer.id = :customerId AND id = :id")
    Optional<Reservation> findByCustomerIdAndId(@Param("customerId") Long customerId, @Param("id") Long id);

    @Query(value = "SELECT r FROM reservation r WHERE r.departmentTake.id = :departmentId AND r.customer.id = :customerId ORDER BY r.id DESC")
    List<Reservation> findAllByCustomerIdAndDepartmentTakeId(@Param("customerId") Long customerId, @Param("departmentId") Long departmentId);

    @Query(value = "SELECT r FROM reservation r WHERE r.departmentBack.id = :departmentId AND r.customer.id = :customerId ORDER BY r.id DESC")
    List<Reservation> findAllByCustomerIdAndDepartmentBackId(@Param("customerId") Long customerId, @Param("departmentId") Long departmentId);

    @Query(value = "SELECT r FROM reservation r WHERE r.customer.id = :customerId AND r.status IN (1, 3) ")
    List<Reservation> findAllActiveByCustomerId(@Param("customerId") Long customerId);
}
