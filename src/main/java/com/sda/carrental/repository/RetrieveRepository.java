package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.department.Department;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RetrieveRepository extends CrudRepository<Retrieve, Long> {

    @Query("SELECT r FROM retrieve r " +
            "WHERE r.department IN (:departments) " +
            "AND r.id IN " +
            "(SELECT p.reservationId FROM payment_details p WHERE p.deposit <> 0) " +
            "ORDER BY r.dateTo ASC")
    List<Retrieve> findAllUnresolvedByDepartments(@Param("departments") List<Department> departments);

    @Query("SELECT r FROM retrieve r " +
            "JOIN customer u ON u.id = r.rent.reservation.customer " +
            "JOIN car c ON c.id = r.rent.car " +
            "WHERE r.department IN (:departments) " +
            "AND (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT(:name, '%'))) " +
            "AND (:surname IS NULL OR LOWER(u.surname) LIKE LOWER(CONCAT(:surname, '%'))) " +
            "AND (:country IS NULL OR (:country = '' OR LOWER(c.plate) LIKE LOWER(CONCAT(:country, '-%')))) " +
            "AND (:plate IS NULL OR LOWER(c.plate) LIKE LOWER(CONCAT('%-%', :plate, '%'))) " +
            "AND r.id IN " +
            "(SELECT p.reservationId FROM payment_details p WHERE p.deposit <> 0) " +
            "ORDER BY r.dateTo ASC"
    )
    List<Retrieve> findUnresolved(@Param("name") String name, @Param("surname") String surname,
                                  @Param("country") String country, @Param("plate") String plate,
                                  @Param("departments") List<Department> departments);

    @Query("SELECT r FROM retrieve r " +
            "WHERE r.rent.car = :car " +
            "ORDER BY r.rent.dateFrom DESC")
    List<Retrieve> findRetrievalsByCar(@Param("car") Car car, Pageable pageable);

    @Query("SELECT r FROM retrieve r " +
            "JOIN customer u ON u.id = r.rent.reservation.customer " +
            "JOIN car c ON c.id = r.rent.car " +
            "WHERE ((:isArrival = true AND r.department IN (:departments)) OR (:isArrival = false AND r.rent.reservation.departmentTake IN (:departments))) " +
            "AND (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT(:name, '%'))) " +
            "AND (:surname IS NULL OR LOWER(u.surname) LIKE LOWER(CONCAT(:surname, '%'))) " +
            "AND (:country IS NULL OR (:country = '' OR LOWER(c.plate) LIKE LOWER(CONCAT(:country, '-%')))) " +
            "AND (:plate IS NULL OR LOWER(c.plate) LIKE LOWER(CONCAT('%-%', :plate, '%'))) " +
            "AND (:dateFrom IS NULL OR :dateFrom <= r.dateTo) " +
            "AND (:dateTo IS NULL OR :dateTo >= r.dateTo) " +
            "ORDER BY r.dateTo DESC"
    )
    List<Retrieve> findRetrievedByCriteria(@Param("name") String name, @Param("surname") String surname,
                                           @Param("country") String country, @Param("plate") String plate,
                                           @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo,
                                           @Param("departments") List<Department> departments, @Param("isArrival") boolean isArrival);
}
