package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.repository.CarBaseRepository;
import com.sda.carrental.web.mvc.form.property.cars.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CarBaseService {
    private final SettingsService settingsService;
    private final CarBaseRepository repository;
    private final DepartmentService departmentService;

    public List<CarBase> findAllSorted() {
        return repository.findAllAndSort();
    }

    public CarBase findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<CarBase> findAvailableCarBasesInCountry(LocalDate dateFrom, LocalDate dateTo, Country country) {
        int reservationGap = settingsService.getInstance().getReservationGap();

        //Currently queries list of cars for customer to receive - assuming overbooking is not allowed
        return repository.findAvailableCarBasesInCountry(dateFrom.minusDays(reservationGap), dateTo.plusDays(reservationGap), country);
    }

    public Map<String, Object> getFilterProperties(List<CarBase> carBaseList, boolean isExpanded) {
        Set<String> brands = new HashSet<>();
        Set<CarBase.CarType> types = new HashSet<>();
        Set<Integer> seats = new HashSet<>();

        for (CarBase carBase : carBaseList) {
            brands.add(carBase.getBrand());
            types.add(carBase.getCarType());
            seats.add(carBase.getSeats());
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

        if (isExpanded) {
            Set<Integer> years = new HashSet<>();
            for (CarBase carBase : carBaseList) {
                years.add(carBase.getYear());
            }
            List<Integer> sortedYears = new ArrayList<>(years);
            sortedYears.sort(null);
            carProperties.put("years", sortedYears);
        }

        return carProperties;
    }

    public List<CarBase> findCarBasesByForm(GenericCarForm form) throws ResourceNotFoundException {
        List<CarBase> carBases;
        Department department = null;
        if (form instanceof SelectCarBaseFilterForm f) {
            department = departmentService.findById(f.getIndexData().getDepartmentIdFrom());
            carBases = findAvailableCarBasesInCountry(
                    f.getIndexData().getDateFrom(),
                    f.getIndexData().getDateTo(),
                    department.getCountry());
        } else if (form instanceof SubstituteCarBaseFilterForm f) {
            department = departmentService.findById(f.getDepartmentId());
            carBases = findAvailableCarBasesInDepartment(department.getId());
        } else if (form instanceof SearchCarBasesFilterForm f) {
            carBases = findCarBasesByExpandedCriteria(f.getDepositMin(), f.getDepositMax(), f.getYears());
        } else {
            return Collections.emptyList();
        }

        return applyFilters(carBases, form, department);
    }

    private List<CarBase> applyFilters(List<CarBase> cbl, GenericCarForm form, Department department) {
        Double multiplier;
        if (department != null) {
            multiplier = department.getCountry().getCurrency().getExchange() * department.getMultiplier();
        } else {
            multiplier = 1D;
        }

        if (form.getPriceMin() != null) {
            cbl.removeIf(carBase -> (carBase.getPriceDay() * multiplier) < form.getPriceMin());
        }

        if (form.getPriceMax() != null) {
            cbl.removeIf(carBase -> (carBase.getPriceDay() * multiplier) > form.getPriceMax());
        }

        if (!form.getBrands().isEmpty()) {
            cbl.removeIf(carBase -> !form.getBrands().contains(carBase.getBrand()));
        }

        if (!form.getSeats().isEmpty()) {
            cbl.removeIf(carBase -> !form.getSeats().contains(carBase.getSeats()));
        }

        if (!form.getTypes().isEmpty()) {
            cbl.removeIf(carBase -> !form.getTypes().contains(carBase.getCarType()));
        }

        return cbl;
    }

    public List<CarBase> findAvailableCarBasesInDepartment(Long department) {
        return repository.findAvailableCarBasesInDepartment(department);
    }

    public CarBase findAvailableCarBaseInDepartment(Long carBaseId, Long department) throws ResourceNotFoundException {
        return repository.findAvailableCarBaseInDepartment(carBaseId, department).orElseThrow(ResourceNotFoundException::new);
    }

    public List<CarBase> findCarBasesByExpandedCriteria(Double depositMin, Double depositMax, List<Integer> years) {
        return repository.findByDepositAndModelYear(depositMin, depositMax, years);
    }

    @Transactional
    public HttpStatus register(RegisterCarBaseForm form) {
        try {
            String image = "/cars/i30.png"; // Car base image hardcoded due to absence of external storage system and need of demo
            repository.save(new CarBase(image, form.getBrand(), form.getModel(), form.getYear(), CarBase.CarType.valueOf(form.getType()), form.getSeats(), form.getPrice(), form.getDeposit()));
            return HttpStatus.CREATED;
        } catch (RuntimeException e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Transactional
    public HttpStatus handleCarBaseDelete(CarBase carBase) {
        try {
            Optional<CarBase> cb = repository.findByIdAndNoCars(carBase.getId());
            if (cb.isEmpty()) return HttpStatus.PRECONDITION_FAILED;

            repository.delete(carBase);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus handleCarBaseImageUpdate(CarBase cb, MultipartFile image) {
        try {
            // Method would delete old file and upload new file to the external storage system
            // After successfully replacing files it would update CarBase in database with new image reference
            repository.save(cb);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus handleCarBasePricesUpdate(CarBase cb, UpdateCarBasePricesForm form) {
        try {
            cb.setPriceDay(Double.parseDouble(form.getPrice()));
            cb.setDepositValue(Double.parseDouble(form.getDeposit()));
            repository.save(cb);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Map<String, long[]> getStatistics(long departmentId) {
        List<Object[]> results = repository.getCarBaseStatisticsByDepartment(departmentId, LocalDate.now().plusWeeks(1));

        Map<String, long[]> statistics = new HashMap<>();
        for (Object[] result:results) {
            long[] counts = new long[7];
            counts[0] = ((BigDecimal) result[1]).longValue(); // Open
            counts[1] = ((BigDecimal) result[2]).longValue(); // Rented
            counts[2] = ((BigDecimal) result[3]).longValue(); // Unavailable
            counts[3] = ((BigInteger) result[4]).longValue(); // Dep (Week)
            counts[4] = ((BigInteger) result[5]).longValue(); // Dep (All)
            counts[5] = ((BigInteger) result[6]).longValue(); // Arr (Week)
            counts[6] = ((BigInteger) result[7]).longValue(); // Arr (All)

            statistics.put(String.valueOf(result[0]), counts);
        }

        return statistics;
    }
}
