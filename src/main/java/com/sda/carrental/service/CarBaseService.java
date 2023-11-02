package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.repository.CarBaseRepository;
import com.sda.carrental.web.mvc.form.CarFilterForm;
import com.sda.carrental.web.mvc.form.GenericCarForm;
import com.sda.carrental.web.mvc.form.SubstituteCarFilterForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CarBaseService {
    private final ConstantValues cv;
    private final CarBaseRepository repository;
    private final DepartmentService departmentService;

    public List<CarBase> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public CarBase findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<CarBase> getAvailableCarBasesInCountry(LocalDate dateFrom, LocalDate dateTo, Country country) {
        return repository.getAvailableCarBasesInCountry(dateFrom.minusDays(cv.getReservationGap()), dateTo.plusDays(cv.getReservationGap()), country);
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

    public List<CarBase> findAvailableCarBasesByForm(GenericCarForm form) {
        List<CarBase> carBases;
        if (form instanceof CarFilterForm f) {
            carBases = repository.getAvailableCarBasesInCountry(
                    f.getIndexData().getDateFrom(),
                    f.getIndexData().getDateTo(),
                    departmentService.findDepartmentWhereId(f.getIndexData().getDepartmentIdFrom()).getCountry());
        } else if (form instanceof SubstituteCarFilterForm f) {
            carBases = repository.getAvailableCarBasesInDepartment(f.getDepartmentId());
        } else {
            return Collections.emptyList();
        }

        return applyFilters(carBases, form);
    }

    private List<CarBase> applyFilters(List<CarBase> cbl, GenericCarForm form) {
        if (form.getPriceMin() != null) {
            cbl.removeIf(carBase -> carBase.getPriceDay() < form.getPriceMin());
        }

        if (form.getPriceMax() != null) {
            cbl.removeIf(carBase -> carBase.getPriceDay() > form.getPriceMax());
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

    public List<CarBase> getAvailableCarBasesInDepartment(Long department) {
        return repository.getAvailableCarBasesInDepartment(department);
    }

    public CarBase getAvailableCarBaseInDepartment(Long carBaseId, Long department) throws ResourceNotFoundException {
        return repository.getAvailableCarBaseInDepartment(carBaseId, department).orElseThrow(ResourceNotFoundException::new);
    }
}
