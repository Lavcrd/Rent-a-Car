package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RetrieveRepository extends CrudRepository<Retrieve, Long> {

    @Query("SELECT r FROM retrieve r " +
            "WHERE r.reservation.departmentBack IN (:departments) " +
            "AND r.reservation.id IN " +
            "(SELECT p.reservation.id FROM payment_details p WHERE p.deposit <> 0) " +
            "ORDER BY r.dateTo ASC")
    List<Retrieve> findAllUnresolvedByDepartments(@Param("departments") List<Department> departments);

    @Query("SELECT r FROM retrieve r " +
            "JOIN customer u ON u.id = r.reservation.customer " +
            "JOIN car c ON c.id = r.reservation.car " +
            "WHERE r.reservation.departmentBack IN (:departments) " +
            "AND (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT(:name, '%'))) " +
            "AND (:surname IS NULL OR LOWER(u.surname) LIKE LOWER(CONCAT(:surname, '%'))) " +
            "AND (:country IS NULL OR LOWER(c.plate) LIKE LOWER(CONCAT(:country, '-%'))) " +
            "AND (:plate IS NULL OR LOWER(c.plate) LIKE LOWER(CONCAT('%-%', :plate, '%'))) " +
            "AND r.reservation.id IN " +
            "(SELECT p.reservation.id FROM payment_details p WHERE p.deposit <> 0) " +
            "ORDER BY r.dateTo ASC"
    )
    List<Retrieve> findUnresolved(@Param("name") String name, @Param("surname") String surname,
                                  @Param("country") String country, @Param("plate") String plate,
                                  @Param("departments") List<Department> departments);
}
