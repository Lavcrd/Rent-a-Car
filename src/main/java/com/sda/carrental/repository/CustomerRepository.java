package com.sda.carrental.repository;

import com.sda.carrental.model.users.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    @Query(value = "SELECT c FROM customer c JOIN reservation r ON c.id = r.customer.id " +
            "WHERE r.departmentTake.departmentId = :departmentId " +
            "AND (:name IS NULL OR LOWER(c.name) = LOWER(:name)) " +
            "AND (:surname IS NULL OR LOWER(c.surname) = LOWER(:surname)) " +
            "AND (c.status <> 2) " +
            "GROUP BY c.id")
    List<Customer> findCustomersByDepartmentAndName(@Param("departmentId") Long departmentId, @Param("name") String name, @Param("surname") String surname);
}
