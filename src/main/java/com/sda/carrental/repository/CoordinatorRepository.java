package com.sda.carrental.repository;

import com.sda.carrental.model.users.Coordinator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoordinatorRepository extends CrudRepository<Coordinator, Long> {

    @Query(value = "FROM coordinator c WHERE c.id=:id")
    Optional<Coordinator> findCoordinatorById(@Param("id") Long id);
}
