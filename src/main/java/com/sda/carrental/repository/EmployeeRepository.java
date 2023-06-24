package com.sda.carrental.repository;

import com.sda.carrental.model.users.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {

    @Query(value = "FROM employee e WHERE e.id=:id")
    Optional<Employee> findEmployeeById(@Param("id") Long id);
}
