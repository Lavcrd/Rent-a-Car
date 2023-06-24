package com.sda.carrental.repository;

import com.sda.carrental.model.users.Manager;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ManagerRepository extends CrudRepository<Manager, Long> {

    @Query(value = "FROM manager m WHERE m.id=:id")
    Optional<Manager> findManagerById(@Param("id") Long id);
}
