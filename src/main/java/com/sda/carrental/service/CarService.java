package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.repository.CarRepository;
import com.sda.carrental.web.mvc.form.CarFilterForm;
import com.sda.carrental.web.mvc.form.SubstituteCarFilterForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository repository;
    private final ConstantValues cv;

    public Car findCarById(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));
    }

    public List<Car> findAvailableDistinctCarsInDepartment(LocalDate dateFrom, LocalDate dateTo, Long department) {
        return repository.findAvailableDistinctCarsInDepartment(dateFrom.minusDays(cv.getReservationGap()), dateTo.plusDays(cv.getReservationGap()), department);
    }

    public List<Car> findAvailableCarsInDepartment(Reservation r) {
        return repository.findAvailableCarsInDepartment(r.getDateFrom().minusDays(cv.getReservationGap()), r.getDateTo().plusDays(cv.getReservationGap()), r.getDepartmentTake().getDepartmentId());
    }

    public Car findAvailableCar(LocalDate dateFrom, LocalDate dateTo, Long department, long carId) {
        return repository.findCarByCarIdAndAvailability(dateFrom.minusDays(cv.getReservationGap()), dateTo.plusDays(cv.getReservationGap()), department, carId).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Car> filterCars(CarFilterForm filterForm) {
        ArrayList<Car> filteredCars = (ArrayList<Car>) repository.findAvailableDistinctCarsInDepartment(
                filterForm.getIndexData().getDateFrom().minusDays(cv.getReservationGap()),
                filterForm.getIndexData().getDateTo().plusDays(cv.getReservationGap()),
                filterForm.getIndexData().getDepartmentIdFrom());

        if (filterForm.getPriceMin() != null) {
            filteredCars.removeIf(car -> car.getPriceDay() < filterForm.getPriceMin());
        }

        if (filterForm.getPriceMax() != null) {
            filteredCars.removeIf(car -> car.getPriceDay() > filterForm.getPriceMax());
        }

        if (filterForm.getBrands() != null) {
            filteredCars.removeIf(car -> !filterForm.getBrands().contains(car.getBrand()));
        }

        if (filterForm.getSeats() != null) {
            filteredCars.removeIf(car -> !filterForm.getSeats().contains(car.getSeats()));
        }

        if (filterForm.getTypes() != null) {
            filteredCars.removeIf(car -> !filterForm.getTypes().contains(car.getCarType()));
        }

        return filteredCars;
    }

    public List<Car> filterCars(SubstituteCarFilterForm filterForm) {
        ArrayList<Car> filteredCars = (ArrayList<Car>) repository.findAvailableCarsInDepartment(
                filterForm.getDateFrom(),
                filterForm.getDateTo().plusDays(cv.getReservationGap()),
                filterForm.getDepartmentId());

        if (filterForm.getPriceMin() != null) {
            filteredCars.removeIf(car -> car.getPriceDay() < filterForm.getPriceMin());
        }

        if (filterForm.getPriceMax() != null) {
            filteredCars.removeIf(car -> car.getPriceDay() > filterForm.getPriceMax());
        }

        if (filterForm.getBrands() != null) {
            filteredCars.removeIf(car -> !filterForm.getBrands().contains(car.getBrand()));
        }

        if (filterForm.getSeats() != null) {
            filteredCars.removeIf(car -> !filterForm.getSeats().contains(car.getSeats()));
        }

        if (filterForm.getTypes() != null) {
            filteredCars.removeIf(car -> !filterForm.getTypes().contains(car.getCarType()));
        }

        return filteredCars;
    }

    public Map<String, Object> getFilterProperties(List<Car> carList) {
        Set<String> brands = new HashSet<>();
        Set<Car.CarType> types = new HashSet<>();
        Set<Integer> seats = new HashSet<>();

        for (Car car : carList) {
            brands.add(car.getBrand());
            types.add(car.getCarType());
            seats.add(car.getSeats());
        }

        List<String> sortedBrands = new ArrayList<>(brands);
        sortedBrands.sort(null);
        List<Car.CarType> sortedTypes = new ArrayList<>(types);
        sortedTypes.sort(null);
        List<Integer> sortedSeats = new ArrayList<>(seats);
        sortedSeats.sort(null);

        Map<String, Object> carProperties = new HashMap<>();
        carProperties.put("brand", sortedBrands);
        carProperties.put("type", sortedTypes);
        carProperties.put("seats", sortedSeats);
        return carProperties;
    }

    @Transactional
    public void updateCarStatus(Car car, Car.CarStatus status) {
        car.setCarStatus(status);
        repository.save(car);
    }

    @Transactional
    public void updateCarLocation(Car car, Long departmentBack) {
        car.setDepartmentId(departmentBack);
        repository.save(car);
    }
}