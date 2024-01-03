package com.sda.carrental.repository;

import com.sda.carrental.model.users.Coordinator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoordinatorRepository extends CrudRepository<Coordinator, Long> {

    @Query(value = "SELECT c FROM coordinator c WHERE c.id=:id")
    Optional<Coordinator> findCoordinatorById(@Param("id") Long id);

    @Query("SELECT c FROM coordinator c " +
            "WHERE (:expired = CASE WHEN CURRENT_DATE >= c.terminationDate THEN true ELSE false END) " +
            "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT(:name, '%'))) " +
            "AND (:surname IS NULL OR LOWER(c.surname) LIKE LOWER(CONCAT(:surname, '%'))) " +
            "AND (SELECT d FROM department d WHERE d.id = :department) MEMBER OF c.departments")
    List<Coordinator> findAllByForm(@Param("name") String name, @Param("surname") String surname, @Param("department") Long department, @Param("expired") boolean expired);
}
