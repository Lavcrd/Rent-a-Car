package com.sda.carrental.repository;

import com.sda.carrental.model.property.payments.PaymentDetails;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PaymentDetailsRepository extends CrudRepository<PaymentDetails, Long> {

    @Query(value = "SELECT p FROM payment_details p WHERE p.reservationId = :operationId")
    Optional<PaymentDetails> findByOperationId(@Param("operationId") Long operationId);

    @Query(value = "SELECT " +
            "    SUM(CASE WHEN r1.status IN (0, 1) THEN pd.payment_accepted ELSE 0 END) AS rental_payment, " +
            "    SUM(CASE WHEN r1.status = '5' THEN pd.payment_accepted ELSE 0 END) AS cancellation_payment, " +
            "    SUM(CASE WHEN r1.status IN (0, 1) THEN " +
            "               CASE WHEN pd.initial_car_fee + pd.initial_divergence_fee > pd.payment_accepted THEN pd.payment_accepted - (pd.initial_car_fee + pd.initial_divergence_fee) ELSE 0 END  " +
            "            ELSE 0 END) AS additional_negative, " +
            "    SUM(CASE WHEN (r1.status IN (0, 1) OR r1.status = '5') THEN " +
            "               CASE WHEN pd.initial_car_fee + pd.initial_divergence_fee < pd.payment_accepted THEN pd.payment_accepted - (pd.initial_car_fee + pd.initial_divergence_fee) ELSE 0 END " +
            "            ELSE 0 END) AS additional_positive, " +
            "    SUM(CASE WHEN r1.status IN (0, 1) THEN pd.initial_car_fee ELSE 0 END) AS car_fees, " +
            "    SUM(CASE WHEN r1.status IN (0, 1) THEN pd.initial_divergence_fee ELSE 0 END) AS divergence_fees " +
            "FROM payment_details pd " +
            "LEFT JOIN reservation r1 ON pd.reservation_id = r1.id " +
            "WHERE r1.date_from >= :dateFrom " +
            "    AND r1.date_from <= :dateTo " +
            "    AND r1.department_id IN (:departmentId);"
            , nativeQuery = true)
    Object getDepartmentStatistics(@Param("departmentId") Long departmentId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
}