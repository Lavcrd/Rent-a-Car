package com.sda.rentacar.repository;

import com.sda.rentacar.model.users.auth.Credentials;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CredentialsRepository extends CrudRepository<Credentials, Long> {
    Optional<Credentials> findByUsername(String username);

    @Query(value = "SELECT c.password FROM credentials c WHERE c.id=:id")
    Optional<String> getPasswordById(@Param("id") Long id);
}
