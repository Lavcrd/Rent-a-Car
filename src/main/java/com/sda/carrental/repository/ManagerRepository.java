package com.sda.carrental.repository;

import com.sda.carrental.model.users.Manager;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ManagerRepository extends CrudRepository<Manager, Long> {

    @Query(value = "SELECT m FROM manager m WHERE m.id=:id")
    Optional<Manager> findManagerById(@Param("id") Long id);

    @Query("SELECT m FROM manager m " +
            "WHERE (:expired = CASE WHEN CURRENT_DATE >= m.terminationDate THEN true ELSE false END) " +
            "AND (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT(:name, '%'))) " +
            "AND (:surname IS NULL OR LOWER(m.surname) LIKE LOWER(CONCAT(:surname, '%'))) " +
            "AND (:department IS NULL OR :department = m.department.id)")
    List<Manager> findAllByForm(@Param("name") String name, @Param("surname") String surname, @Param("department") Long department, @Param("expired") boolean expired);
}
