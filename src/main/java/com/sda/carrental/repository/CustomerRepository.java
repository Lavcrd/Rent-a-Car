package com.sda.carrental.repository;

import com.sda.carrental.model.users.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    @Query(value = "SELECT c FROM customer c JOIN reservation r ON c.id = r.customer.id " +
            "WHERE r.departmentTake.departmentId = :departmentId " +
            "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT(:name, '%'))) " +
            "AND (:surname IS NULL OR LOWER(c.surname) LIKE LOWER(CONCAT(:surname, '%'))) " +
            "AND (c.status <> 2) " +
            "GROUP BY c.id")
    List<Customer> findCustomersByDepartmentAndName(@Param("departmentId") Long departmentId, @Param("name") String name, @Param("surname") String surname);

    @Query(value = "SELECT c FROM verification v LEFT JOIN customer c ON c.id = v.customerId " +
            "WHERE v.personalId = :personalId AND v.driverId = :driverId")
    Optional<Customer> findByVerification(@Param("personalId") String personalId, @Param("driverId") String driverId);
}
