package com.sda.carrental.repository;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.users.auth.Verification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VerificationRepository extends CrudRepository<Verification, Long> {
    @Query(value = "SELECT v FROM verification v WHERE v.personalId = :personalId AND v.country = :country")
    Optional<Verification> findByVerificationFields(@Param("country") Country country, @Param("personalId") String personalId);
}
