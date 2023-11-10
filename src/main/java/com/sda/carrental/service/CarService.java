package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.repository.CarRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.property.cars.GenericCarForm;
import com.sda.carrental.web.mvc.form.property.cars.RegisterCarForm;
import com.sda.carrental.web.mvc.form.property.cars.SearchCarsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository repository;
    private final CarBaseService carBaseService;
    private final DepartmentService departmentService;

    public Car findCarById(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Car", "id", id));
    }

    public List<Car> findAvailableCarsInDepartment(Reservation r) {
        return repository.findAvailableCarsInDepartment(r.getCarBase(), r.getDepartmentTake().getId());
    }

    public Car findAvailableCar(long carId, long departmentId) throws ResourceNotFoundException {
        return repository.findCarByIdAndAvailability(carId, departmentId).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Car> findByCriteria(SearchCarsForm f) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Country formCountry = Country.valueOf(f.getCountry());
        String country;
        if (formCountry.equals(Country.COUNTRY_NONE)) {
            country = null;
        } else {
            country = formCountry.getCode();
        }

        List<Department> departments;
        if (f.getDepartment() == null) {
            departments = departmentService.getDepartmentsByUserContext(cud);
        } else {
            if (departmentService.departmentAccess(cud, f.getDepartment()).equals(HttpStatus.FORBIDDEN))
                return Collections.emptyList();
            departments = List.of(departmentService.findDepartmentWhereId(f.getDepartment()));
        }

        Car.CarStatus carStatus;
        if (f.getStatus().isEmpty()) {
            carStatus = null;
        } else {
            carStatus = Car.CarStatus.valueOf(f.getStatus());
        }

        List<Car> cars = repository.findByCriteria(
                f.getMileageMin(), f.getMileageMax(),
                country, f.getPlate(),
                departments, carStatus);

        return applyFilters(cars, f);
    }

    private List<Car> applyFilters(List<Car> cl, GenericCarForm form) {
        if (form.getPriceMin() != null) {
            cl.removeIf(car -> car.getCarBase().getPriceDay() < form.getPriceMin());
        }

        if (form.getPriceMax() != null) {
            cl.removeIf(car -> car.getCarBase().getPriceDay() > form.getPriceMax());
        }

        if (!form.getBrands().isEmpty()) {
            cl.removeIf(car -> !form.getBrands().contains(car.getCarBase().getBrand()));
        }

        if (!form.getSeats().isEmpty()) {
            cl.removeIf(car -> !form.getSeats().contains(car.getCarBase().getSeats()));
        }

        if (!form.getTypes().isEmpty()) {
            cl.removeIf(car -> !form.getTypes().contains(car.getCarBase().getCarType()));
        }

        return cl;
    }

    public Map<String, Object> getFilterProperties(List<Car> carList) {
        Set<String> brands = new HashSet<>();
        Set<CarBase.CarType> types = new HashSet<>();
        Set<Integer> seats = new HashSet<>();

        for (Car car : carList) {
            brands.add(car.getCarBase().getBrand());
            types.add(car.getCarBase().getCarType());
            seats.add(car.getCarBase().getSeats());
        }

        List<String> sortedBrands = new ArrayList<>(brands);
        sortedBrands.sort(null);
        List<CarBase.CarType> sortedTypes = new ArrayList<>(types);
        sortedTypes.sort(null);
        List<Integer> sortedSeats = new ArrayList<>(seats);
        sortedSeats.sort(null);

        Map<String, Object> carProperties = new HashMap<>();
        carProperties.put("brands", sortedBrands);
        carProperties.put("types", sortedTypes);
        carProperties.put("seats", sortedSeats);
        return carProperties;
    }

    public boolean isCarUnavailable(Car c) {
        return !c.getCarStatus().equals(Car.CarStatus.STATUS_OPEN);
    }

    @Transactional
    public HttpStatus updateCarStatus(Car car, Car.CarStatus status) {
        car.setCarStatus(status);
        repository.save(car);
        return HttpStatus.OK;
    }

    @Transactional
    public HttpStatus updateCarLocation(Car car, Department department) {
        car.setDepartment(department);
        repository.save(car);
        return HttpStatus.OK;
    }

    @Transactional
    public HttpStatus updateCarMileage(Car car, Long mileage) {
        car.setMileage(mileage);
        repository.save(car);
        return HttpStatus.OK;
    }

    @Transactional
    public void retrieveCar(Car car, Department department, Long mileage) {
        updateCarStatus(car, Car.CarStatus.STATUS_OPEN);
        updateCarLocation(car, department);
        updateCarMileage(car, mileage);
    }

    public List<Car> findByDepartments(List<Department> departments) {
        return repository.findAllByDepartments(departments);
    }

    public Car findByOperationId(Long operationId) throws ResourceNotFoundException {
        return repository.findByOperationId(operationId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public HttpStatus register(RegisterCarForm form) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (departmentService.departmentAccess(cud, form.getDepartment()).equals(HttpStatus.FORBIDDEN)) throw new RuntimeException();

            CarBase carBase = carBaseService.findById(form.getPattern());
            Department department = departmentService.findDepartmentWhereId(form.getDepartment());

            repository.save(new Car(carBase, department, department.getCountry().getCode() + "-" + form.getPlate().toUpperCase(), form.getMileage(), Car.CarStatus.STATUS_UNAVAILABLE));
            return HttpStatus.CREATED;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    public HttpStatus handleStatus(Car c, Car.CarStatus s) {
        if (s.equals(Car.CarStatus.STATUS_OPEN)) {
            Optional<Car> car = repository.findRentedCarById(c.getId());
            if (car.isPresent()) return HttpStatus.PRECONDITION_FAILED;
        }
        return updateCarStatus(c, s);
    }
}