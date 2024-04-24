package com.sda.rentacar.repository;

import com.sda.rentacar.model.property.payments.PaymentDetails;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PaymentDetailsRepository extends CrudRepository<PaymentDetails, Long> {

    @Query(value = "SELECT p FROM payment_details p WHERE p.reservationId = :operationId")
    Optional<PaymentDetails> findByOperationId(@Param("operationId") Long operationId);

    @Query(value = "SELECT " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN pd.payment_accepted ELSE 0 END) AS rental_payment, " +
            "    SUM(CASE WHEN r.status = '5' THEN pd.payment_accepted ELSE 0 END) AS cancellation_payment, " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN " +
            "               CASE WHEN pd.initial_car_fee + pd.initial_divergence_fee > pd.payment_accepted THEN pd.payment_accepted - (pd.initial_car_fee + pd.initial_divergence_fee) ELSE 0 END  " +
            "            ELSE 0 END) AS additional_negative, " +
            "    SUM(CASE WHEN (r.status IN (0, 1) OR r.status = '5') THEN " +
            "               CASE WHEN pd.initial_car_fee + pd.initial_divergence_fee < pd.payment_accepted THEN pd.payment_accepted - (pd.initial_car_fee + pd.initial_divergence_fee) ELSE 0 END " +
            "            ELSE 0 END) AS additional_positive, " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN pd.initial_car_fee ELSE 0 END) AS car_fees, " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN pd.initial_divergence_fee ELSE 0 END) AS divergence_fees " +
            "FROM payment_details pd " +
            "LEFT JOIN reservation r ON pd.reservation_id = r.id " +
            "WHERE r.date_from >= :dateFrom " +
            "    AND r.date_from <= :dateTo " +
            "    AND r.department_id IN (:departmentId);"
            , nativeQuery = true)
    Object getDepartmentServiceStatistics(@Param("departmentId") Long departmentId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query(value = "SELECT " +
            "    SUM(pd.deposit) AS pending_deposit, " +
            "    SUM(pd.released_deposit) AS released_deposit, " +
            "    SUM(pd.secured - pd.payment_accepted) AS charged_deposit " +
            "FROM payment_details pd " +
            "LEFT JOIN retrieve r ON pd.reservation_id = r.id " +
            "WHERE r.actual_date_end >= :dateFrom " +
            "    AND r.actual_date_end <= :dateTo " +
            "    AND r.department_id IN (:departmentId);"
    , nativeQuery = true)
    Object getDepartmentDepositStatistics(@Param("departmentId") Long departmentId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query(value = "SELECT " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN pd.payment_accepted ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS account_rental_payment, " +
            "    SUM(CASE WHEN r.status = '5' THEN pd.payment_accepted ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS account_cancellation_payment, " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN " +
            "               CASE WHEN pd.initial_car_fee + pd.initial_divergence_fee > pd.payment_accepted THEN pd.payment_accepted - (pd.initial_car_fee + pd.initial_divergence_fee) ELSE 0 END  " +
            "            ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS account_additional_negative, " +
            "    SUM(CASE WHEN (r.status IN (0, 1) OR r.status = '5') THEN " +
            "               CASE WHEN pd.initial_car_fee + pd.initial_divergence_fee < pd.payment_accepted THEN pd.payment_accepted - (pd.initial_car_fee + pd.initial_divergence_fee) ELSE 0 END " +
            "            ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS account_additional_positive, " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN pd.initial_car_fee ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS account_car_fees, " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN pd.initial_divergence_fee ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS account_divergence_fees " +
            "FROM payment_details pd " +
            "LEFT JOIN reservation r ON pd.reservation_id = r.id " +
            "WHERE r.date_from >= :dateFrom " +
            "    AND r.date_from <= :dateTo " +
            "    AND r.department_id IN (SELECT ed.departments_id FROM employee_departments ed WHERE ed.employee_id = :employeeId);"
            , nativeQuery = true)
    Object getAccountServiceStatistics(@Param("employeeId") Long employeeId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query(value = "SELECT " +
            "    SUM(pd.deposit) / COUNT(DISTINCT(r.department_id)) AS account_pending_deposit, " +
            "    SUM(pd.released_deposit) / COUNT(DISTINCT(r.department_id)) AS account_released_deposit, " +
            "    SUM(pd.secured - pd.payment_accepted) / COUNT(DISTINCT(r.department_id)) AS account_charged_deposit " +
            "FROM payment_details pd " +
            "LEFT JOIN retrieve r ON pd.reservation_id = r.id " +
            "WHERE r.actual_date_end >= :dateFrom " +
            "    AND r.actual_date_end <= :dateTo " +
            "    AND r.department_id IN (SELECT ed.departments_id FROM employee_departments ed WHERE ed.employee_id = :employeeId);"
            , nativeQuery = true)
    Object getAccountDepositStatistics(@Param("employeeId") Long employeeId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query(value = "SELECT " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN pd.payment_accepted ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS global_rental_payment, " +
            "    SUM(CASE WHEN r.status = '5' THEN pd.payment_accepted ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS global_cancellation_payment, " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN " +
            "               CASE WHEN pd.initial_car_fee + pd.initial_divergence_fee > pd.payment_accepted THEN pd.payment_accepted - (pd.initial_car_fee + pd.initial_divergence_fee) ELSE 0 END  " +
            "            ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS global_additional_negative, " +
            "    SUM(CASE WHEN (r.status IN (0, 1) OR r.status = '5') THEN " +
            "               CASE WHEN pd.initial_car_fee + pd.initial_divergence_fee < pd.payment_accepted THEN pd.payment_accepted - (pd.initial_car_fee + pd.initial_divergence_fee) ELSE 0 END " +
            "            ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS global_additional_positive, " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN pd.initial_car_fee ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS global_car_fees, " +
            "    SUM(CASE WHEN r.status IN (0, 1) THEN pd.initial_divergence_fee ELSE 0 END) / COUNT(DISTINCT(r.department_id)) AS global_divergence_fees " +
            "FROM payment_details pd " +
            "LEFT JOIN reservation r ON pd.reservation_id = r.id " +
            "WHERE (r.date_from >= :dateFrom) " +
            "    AND (r.date_from <= :dateTo);"
            , nativeQuery = true)
    Object getGlobalServiceStatistics(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query(value = "SELECT " +
            "    SUM(pd.deposit) / COUNT(DISTINCT(r.department_id)) AS global_pending_deposit, " +
            "    SUM(pd.released_deposit) / COUNT(DISTINCT(r.department_id)) AS global_released_deposit, " +
            "    SUM(pd.secured - pd.payment_accepted) / COUNT(DISTINCT(r.department_id)) AS global_charged_deposit " +
            "FROM payment_details pd " +
            "LEFT JOIN retrieve r ON pd.reservation_id = r.id " +
            "WHERE (r.actual_date_end >= :dateFrom) " +
            "    AND (r.actual_date_end <= :dateTo);"
            , nativeQuery = true)
    Object getGlobalDepositStatistics(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
}