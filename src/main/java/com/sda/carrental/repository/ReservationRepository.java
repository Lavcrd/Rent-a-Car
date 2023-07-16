package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    @Query(value = "FROM reservation WHERE customer.id = :customerId ORDER BY reservationId DESC")
    List<Reservation> findAllByCustomerId(@Param("customerId") Long customerId);

    @Query(value = "FROM reservation WHERE customer.id = :customerId AND reservationId = :id")
    Optional<Reservation> findByCustomerIdAndId(@Param("customerId") Long customerId, @Param("id") Long id);

    @Query(value = "SELECT r FROM reservation r WHERE r.departmentTake.departmentId = :departmentId AND r.customer.id = :customerId ORDER BY r.reservationId DESC")
    List<Reservation> findAllByCustomerIdAndDepartmentTakeId(@Param("customerId") Long customerId, @Param("departmentId") Long departmentId);

    @Query(value = "SELECT r FROM reservation r WHERE r.departmentBack.departmentId = :departmentId AND r.customer.id = :customerId ORDER BY r.reservationId DESC")
    List<Reservation> findAllByCustomerIdAndDepartmentBackId(@Param("customerId") Long customerId, @Param("departmentId") Long departmentId);

    @Query(value = "FROM reservation r WHERE r.customer.id = :customerId AND r.status IN (1, 3) ")
    List<Reservation> findAllActiveByCustomerId(@Param("customerId") Long customerId);

    @Query(value = "SELECT r FROM reservation r JOIN customer c ON c.id = r.customer.id " +
            "WHERE r.departmentTake.departmentId = :departmentTake AND r.dateFrom >= :dateFrom " +
            "AND (:customerName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT(:customerName, '%'))) " +
            "AND (:customerSurname IS NULL OR LOWER(c.surname) LIKE LOWER(CONCAT(:customerSurname, '%'))) " +
            "AND (:departmentBack IS NULL OR r.departmentBack.departmentId = :departmentBack) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:dateTo IS NULL OR r.dateTo <= :dateTo) " +
            "GROUP BY r.id " +
            "ORDER BY r.dateFrom DESC")
    List<Reservation> findReservationByDetails(
            @Param("customerName") String customerName, @Param("customerSurname") String customerSurname,
            @Param("departmentTake") Long departmentTake, @Param("departmentBack") Long departmentBack,
            @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo,
            @Param("status") Reservation.ReservationStatus status);
}
