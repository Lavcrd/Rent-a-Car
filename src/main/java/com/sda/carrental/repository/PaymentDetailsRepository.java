package com.sda.carrental.repository;

import com.sda.carrental.model.property.payments.PaymentDetails;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentDetailsRepository extends CrudRepository<PaymentDetails, Long> {

    @Query(value = "SELECT p FROM payment_details p WHERE p.reservationId = :operationId")
    Optional<PaymentDetails> findByOperationId(@Param("operationId") Long operationId);
}