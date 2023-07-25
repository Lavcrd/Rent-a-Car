package com.sda.carrental.repository;

import com.sda.carrental.model.users.auth.Verification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VerificationRepository extends CrudRepository<Verification, Long> {
    Optional<Verification> findByCustomerId(Long customerId);
    void delete(Verification verification);

    @Query(value = "SELECT v FROM verification v WHERE v.personalId = :personalId AND v.driverId = :driverId")
    Optional<Verification> findByVerificationFields(@Param("personalId") String personalId, @Param("driverId") String driverId);
}
