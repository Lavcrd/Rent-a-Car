package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Returning;
import com.sda.carrental.model.property.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturningRepository extends CrudRepository<Returning, Long> {

    @Query("SELECT r FROM returning r " +
            "WHERE r.reservation.departmentBack IN (:departments) " +
            "AND r.reservation.id IN " +
            "(SELECT p.reservation.id FROM payment_details p WHERE p.deposit <> 0) " +
            "ORDER BY r.dateTo ASC")
    List<Returning> findAllUnresolvedByDepartments(@Param("departments") List<Department> departments);
}
