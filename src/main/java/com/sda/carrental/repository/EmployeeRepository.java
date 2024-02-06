package com.sda.carrental.repository;

import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.users.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {

    @Query(value = "SELECT e FROM employee e WHERE e.id=:id")
    Optional<Employee> findEmployeeById(@Param("id") Long id);

    @Query("SELECT e FROM employee e " +
            "WHERE (:expired = CASE WHEN CURRENT_DATE >= e.terminationDate THEN true ELSE false END) " +
            "AND (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT(:name, '%'))) " +
            "AND (:surname IS NULL OR LOWER(e.surname) LIKE LOWER(CONCAT(:surname, '%'))) " +
            "AND (SELECT d FROM department d WHERE d.id = :department) MEMBER OF e.departments " +
            "AND (:role IS NULL OR :role = e.role) " +
            "ORDER BY e.role, e.name, e.surname")
    List<Employee> findAllByForm(@Param("name") String name, @Param("surname") String surname,
                                 @Param("department") Long department, @Param("expired") boolean expired,
                                 @Param("role") Role role);

    @Query("SELECT COUNT(r1) > 0, COUNT(r2) > 0 FROM rent r1, retrieve r2 WHERE r1.employeeId = :id OR r2.employeeId = :id")
    List<Boolean> hasPresence(@Param("id") Long id);
}
