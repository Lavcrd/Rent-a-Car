package com.sda.carrental.repository;

import java.util.List;
import java.util.Optional;

import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.car.CarBase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface CarRepository extends CrudRepository<Car, Long> {

    @Query(value = "SELECT c FROM car c " +
            "WHERE c.carBase = :carBase " +
            "AND c.department.id = :department " +
            "AND c.carStatus NOT IN (1, 2, 3)")
    List<Car> findAvailableCarsInDepartment(@Param("carBase") CarBase carBase, @Param("department") Long department);

    @Query(value = "SELECT c FROM car c " +
            "WHERE c.id = :carId " +
            "AND c.department.id = :department " +
            "AND c.carStatus NOT IN (1, 2, 3)")
    Optional<Car> findCarByIdAndAvailability(@Param("carId") long carId, @Param("department") long department);

    @Query(value = "SELECT c FROM car c WHERE c.department IN (:departments)")
    List<Car> findAllByDepartments(@Param("departments") List<Department> departments);

    @Query(value = "SELECT c FROM car c " +
            "WHERE c.department IN (:departments) " +
            "AND ((:status IS NULL AND c.carStatus <= 2) OR c.carStatus = :status) " +
            "AND (:mileageMin IS NULL OR c.mileage >= :mileageMin) " +
            "AND (:mileageMax IS NULL OR c.mileage <= :mileageMax) " +
            "AND (:country IS NULL OR LOWER(c.plate) LIKE LOWER(CONCAT(:country, '-%'))) " +
            "AND (:plate IS NULL OR LOWER(c.plate) LIKE LOWER(CONCAT('%-%', :plate, '%'))) " +
            "ORDER BY c.id ASC")
    List<Car> findByCriteria(@Param("mileageMin") Long mileageMin, @Param("mileageMax") Long mileageMax,
                             @Param("country") String country, @Param("plate") String plate,
                             @Param("departments") List<Department> departments, @Param("status") Car.CarStatus status);

    @Query(value = "SELECT r.car FROM rent r " +
            "WHERE r.id = :operationId")
    Optional<Car> findByOperationId(@Param("operationId") Long operationId);

    @Query(value = "SELECT r.car FROM rent r " +
            "WHERE (r.car.id = :id) " +
            "AND (CURRENT_DATE >= r.dateFrom) " +
            "AND r.id NOT IN (" +
            "SELECT r1 FROM retrieve r1 " +
            "WHERE r1.id = r.id)")
    Optional<Car> findRentedCarById(@Param("id") Long id);

    @Query("SELECT COUNT(c) FROM car c WHERE c.carBase.id = :carBaseId")
    Long getCarSizeByCarBase(@Param("carBaseId") Long carBaseId);
}
