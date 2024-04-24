package com.sda.rentacar.repository;

import com.sda.rentacar.model.operational.Reservation;
import com.sda.rentacar.model.users.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    @Query(value = "SELECT c FROM verification v LEFT JOIN customer c ON c.id = v.id " +
            "WHERE v.personalId = :personalId AND v.country.id = :countryId")
    Optional<Customer> findByVerification(@Param("countryId") Long countryId, @Param("personalId") String personalId);

    @Query(value = "SELECT r.customer AS customer, COUNT(DISTINCT r) AS reservations " +
            "FROM reservation r JOIN customer c ON c.id = r.customer.id " +
            "WHERE r.departmentTake.id = :primaryDepartment " +
            "AND r.dateFrom >= :dateFrom " +
            "AND r.dateFrom <= :dateTo " +
            "AND (:customerName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT(:customerName, '%'))) " +
            "AND (:customerSurname IS NULL OR LOWER(c.surname) LIKE LOWER(CONCAT(:customerSurname, '%'))) " +
            "AND (:secondaryDepartment IS NULL OR r.departmentBack.id = :secondaryDepartment) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "GROUP BY r.customer " +
            "ORDER BY r.dateFrom, c.id DESC")
    List<Object[]> findCustomersDepartureCounts(
            @Param("customerName") String customerName, @Param("customerSurname") String customerSurname,
            @Param("primaryDepartment") Long primaryDepartment, @Param("secondaryDepartment") Long secondaryDepartment,
            @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo,
            @Param("status") Reservation.ReservationStatus status);

    @Query(value = "SELECT r.customer AS customer, COUNT(DISTINCT r) AS reservations " +
            "FROM reservation r JOIN customer c ON c.id = r.customer.id " +
            "WHERE r.departmentBack.id = :primaryDepartment " +
            "AND r.dateTo >= :dateFrom " +
            "AND r.dateTo <= :dateTo " +
            "AND (:customerName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT(:customerName, '%'))) " +
            "AND (:customerSurname IS NULL OR LOWER(c.surname) LIKE LOWER(CONCAT(:customerSurname, '%'))) " +
            "AND (:secondaryDepartment IS NULL OR r.departmentTake.id = :secondaryDepartment) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "GROUP BY r.customer " +
            "ORDER BY r.dateFrom, c.id DESC")
    List<Object[]> findCustomersArrivalCounts(
            @Param("customerName") String customerName, @Param("customerSurname") String customerSurname,
            @Param("primaryDepartment") Long primaryDepartment, @Param("secondaryDepartment") Long secondaryDepartment,
            @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo,
            @Param("status") Reservation.ReservationStatus status);
}
