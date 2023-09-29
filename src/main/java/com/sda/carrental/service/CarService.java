package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.repository.CarRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.CarFilterForm;
import com.sda.carrental.web.mvc.form.GenericCarForm;
import com.sda.carrental.web.mvc.form.SearchCarsForm;
import com.sda.carrental.web.mvc.form.SubstituteCarFilterForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository repository;
    private final DepartmentService departmentService;
    private final ConstantValues cv;

    public Car findCarById(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));
    }

    public List<Car> findAvailableDistinctCarsInDepartment(LocalDate dateFrom, LocalDate dateTo, Long department) {
        return repository.findAvailableDistinctCarsInDepartment(dateFrom.minusDays(cv.getReservationGap()), dateTo.plusDays(cv.getReservationGap()), department);
    }

    public List<Car> findAvailableCarsInDepartment(Reservation r) {
        return repository.findAvailableCarsInDepartment(r.getDateFrom().minusDays(cv.getReservationGap()), r.getDateTo().plusDays(cv.getReservationGap()), r.getDepartmentTake().getId());
    }

    public Car findAvailableCar(LocalDate dateFrom, LocalDate dateTo, Long department, long carId) throws IllegalArgumentException {
        return repository.findCarByCarIdAndAvailability(dateFrom.minusDays(cv.getReservationGap()), dateTo.plusDays(cv.getReservationGap()), department, carId).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Car> findCarsByForm(GenericCarForm form) {
        ArrayList<Car> cars;
        if (form instanceof CarFilterForm f) {
            cars = (ArrayList<Car>) repository.findAvailableDistinctCarsInDepartment(
                    f.getIndexData().getDateFrom().minusDays(cv.getReservationGap()),
                    f.getIndexData().getDateTo().plusDays(cv.getReservationGap()),
                    f.getIndexData().getDepartmentIdFrom());
        } else if (form instanceof SubstituteCarFilterForm f) {
            cars = (ArrayList<Car>) repository.findAvailableCarsInDepartment(
                    f.getDateFrom(),
                    f.getDateTo().plusDays(cv.getReservationGap()),
                    f.getDepartmentId());
        } else if (form instanceof SearchCarsForm f) {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String country;
            if (f.getCountry().equals(Country.COUNTRY_NONE)) {
                country = null;
            } else {
                country = f.getCountry().getCode();
            }

            List<Department> departments;
            if (f.getDepartment() == null) {
                departments = departmentService.getDepartmentsByUserContext(cud);
            } else {
                if (departmentService.departmentAccess(cud, f.getDepartment()).equals(HttpStatus.FORBIDDEN))
                    return Collections.emptyList();
                departments = List.of(departmentService.findDepartmentWhereId(f.getDepartment()));
            }

            cars = (ArrayList<Car>) repository.findByCriteria(
                    f.getMileageMin(), f.getMileageMax(),
                    country, f.getPlate(),
                    departments, f.getStatus()
            );
        } else {
            return Collections.emptyList();
        }

        return applyFilters(cars, form);
    }

    private List<Car> applyFilters(List<Car> cl, GenericCarForm form) {
        if (form.getPriceMin() != null) {
            cl.removeIf(car -> car.getPriceDay() < form.getPriceMin());
        }

        if (form.getPriceMax() != null) {
            cl.removeIf(car -> car.getPriceDay() > form.getPriceMax());
        }

        if (!form.getBrands().isEmpty()) {
            cl.removeIf(car -> !form.getBrands().contains(car.getBrand()));
        }

        if (!form.getSeats().isEmpty()) {
            cl.removeIf(car -> !form.getSeats().contains(car.getSeats()));
        }

        if (!form.getTypes().isEmpty()) {
            cl.removeIf(car -> !form.getTypes().contains(car.getCarType()));
        }

        return cl;
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
        carProperties.put("brands", sortedBrands);
        carProperties.put("types", sortedTypes);
        carProperties.put("seats", sortedSeats);
        return carProperties;
    }

    public boolean isCarUnavailable(Reservation r) {
        return r.getCar().getCarStatus().equals(Car.CarStatus.STATUS_UNAVAILABLE);
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

    @Transactional
    public void updateCarMileage(Car car, Long mileage) {
        car.setMileage(mileage);
        repository.save(car);
    }

    @Transactional
    public void retrieveCar(Car car, Long departmentId, Long mileage) {
        updateCarStatus(car, Car.CarStatus.STATUS_OPEN);
        updateCarLocation(car, departmentId);
        updateCarMileage(car, mileage);
    }

    public List<Car> findByDepartments(List<Department> departments) {
        return repository.findAllByDepartments(departments);
    }
}