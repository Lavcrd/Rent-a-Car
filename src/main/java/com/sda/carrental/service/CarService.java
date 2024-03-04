package com.sda.carrental.service;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.department.Department;
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
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository repository;
    private final CarBaseService carBaseService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

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
        List<Department> departments;
        if (f.getDepartment() == null) {
            departments = employeeService.getDepartmentsByUserContext(cud);
        } else {
            if (employeeService.departmentAccess(cud, f.getDepartment()).equals(HttpStatus.FORBIDDEN))
                return Collections.emptyList();
            departments = List.of(departmentService.findById(f.getDepartment()));
        }

        Car.CarStatus carStatus;
        if (f.getStatus().isEmpty()) {
            carStatus = null;
        } else {
            carStatus = Car.CarStatus.valueOf(f.getStatus());
        }

        List<Car> cars = repository.findByCriteria(
                f.getMileageMin(), f.getMileageMax(),
                f.getCountry(), f.getPlate(),
                departments, carStatus);

        return applyFilters(cars, f, departments.get(0));
    }

    private List<Car> applyFilters(List<Car> cl, GenericCarForm form, Department department) {
        Double multiplier;
        if (department != null) {
            multiplier = department.getCountry().getExchange();
        } else {
            multiplier = 1D;
        }

        if (form.getPriceMin() != null) {
            cl.removeIf(car -> (car.getCarBase().getPriceDay() * multiplier) < form.getPriceMin());
        }

        if (form.getPriceMax() != null) {
            cl.removeIf(car -> (car.getCarBase().getPriceDay() * multiplier) > form.getPriceMax());
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
            if (employeeService.departmentAccess(cud, form.getDepartment()).equals(HttpStatus.FORBIDDEN))
                throw new RuntimeException();

            CarBase carBase = carBaseService.findById(form.getPattern());
            Department department = departmentService.findById(form.getDepartment());

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

    public Long getCarSizeByCarBase(Long id) {
        return repository.getCarSizeByCarBase(id);
    }

    @Transactional
    public HttpStatus delete(Car car) {
        try {
            repository.delete(car);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus handleCarDelete(Long id) {
        Optional<Car> c = repository.findCarByIdAndNoRentals(id);
        if (c.isEmpty()) return HttpStatus.PRECONDITION_FAILED;

        return delete(c.get());
    }

    public List<Car> findCarsByDepartmentsAndCarBase(List<Department> departments, CarBase carBase) {
        return repository.findAllByDepartmentsAndCarBase(departments, carBase);
    }

    @Transactional
    public HttpStatus splitCarsToCarBase(CarBase carBase, CarBase targetCarBase, List<Long> carIdList) {
        try {
            if (carBase == null || targetCarBase == null || carIdList.isEmpty()) {
                return HttpStatus.BAD_REQUEST;
            }

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> departments = employeeService.getDepartmentsByUserContext(cud);

            int updatedCount = repository.updateCarsToCarBase(targetCarBase, carIdList, departments, carBase);

            if (updatedCount != carIdList.size()) {
                throw new IllegalActionException();
            }
            return HttpStatus.ACCEPTED;
        } catch (IllegalActionException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.CONFLICT;
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}