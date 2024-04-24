package com.sda.rentacar.repository;

import com.sda.rentacar.model.operational.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    @Query(value = "SELECT r FROM reservation r WHERE r.customer.id = :customerId ORDER BY id DESC")
    List<Reservation> findAllByCustomerId(@Param("customerId") Long customerId);

    @Query(value = "SELECT r FROM reservation r WHERE r.customer.id = :customerId AND id = :id")
    Optional<Reservation> findByCustomerIdAndId(@Param("customerId") Long customerId, @Param("id") Long id);

    @Query(value = "SELECT r FROM reservation r WHERE r.departmentTake.id = :departmentId AND r.customer.id = :customerId ORDER BY r.id DESC")
    List<Reservation> findAllByCustomerIdAndDepartmentTakeId(@Param("customerId") Long customerId, @Param("departmentId") Long departmentId);

    @Query(value = "SELECT r FROM reservation r WHERE r.departmentBack.id = :departmentId AND r.customer.id = :customerId ORDER BY r.id DESC")
    List<Reservation> findAllByCustomerIdAndDepartmentBackId(@Param("customerId") Long customerId, @Param("departmentId") Long departmentId);

    @Query(value = "SELECT r FROM reservation r WHERE r.customer.id = :customerId AND r.status IN (1, 3) ")
    List<Reservation> findAllActiveByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT r FROM reservation r " +
            "WHERE r.departmentTake.id = :department " +
            "AND r.status IN (2, 3) " +
            "ORDER BY r.dateFrom ASC")
    List<Reservation> findExpectedDeparturesByDepartment(@Param("department") Long department);

    @Query(value = "SELECT " +
            "   COUNT(CASE WHEN r.status != '2' THEN 1 ELSE null END) AS reserved_size, " +
            "   COUNT(CASE WHEN r.status IN (0, 1) THEN 1 ELSE null END) AS rented_size, " +
            "   COUNT(CASE WHEN r.status IN (4, 5) THEN 1 ELSE null END) AS cancel_size " +
            "FROM reservation r  " +
            "WHERE r.date_from >= :dateFrom " +
            "   AND r.date_from <= :dateTo " +
            "   AND r.department_id IN (:departmentId);"
    , nativeQuery = true)
    Object getDepartmentReservationStatistics(@Param("departmentId") Long departmentId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query(value = "SELECT " +
            "   COUNT(CASE WHEN r.status != '2' THEN 1 ELSE null END) / COUNT(DISTINCT(r.department_id)) AS account_reserved_size, " +
            "   COUNT(CASE WHEN r.status IN (0, 1) THEN 1 ELSE null END) / COUNT(DISTINCT(r.department_id)) AS account_rented_size, " +
            "   COUNT(CASE WHEN r.status IN (4, 5) THEN 1 ELSE null END) / COUNT(DISTINCT(r.department_id)) AS account_cancel_size " +
            "FROM reservation r  " +
            "WHERE r.date_from >= :dateFrom " +
            "   AND r.date_from <= :dateTo " +
            "   AND r.department_id IN (SELECT ed.departments_id FROM employee_departments ed WHERE ed.employee_id = :employeeId);"
            , nativeQuery = true)
    Object getAccountReservationStatistics(@Param("employeeId") Long employeeId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query(value = "SELECT " +
            "   COUNT(CASE WHEN r.status != '2' THEN 1 ELSE null END) / COUNT(DISTINCT(r.department_id)) AS global_reserved_size, " +
            "   COUNT(CASE WHEN r.status IN (0, 1) THEN 1 ELSE null END) / COUNT(DISTINCT(r.department_id)) AS global_rented_size, " +
            "   COUNT(CASE WHEN r.status IN (4, 5) THEN 1 ELSE null END) / COUNT(DISTINCT(r.department_id)) AS global_cancel_size " +
            "FROM reservation r  " +
            "WHERE (r.date_from >= :dateFrom) " +
            "   AND (r.date_from <= :dateTo);"
            , nativeQuery = true)
    Object getGlobalReservationStatistics(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
}
