package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.Utility;
import com.sda.carrental.model.operational.Rent;
import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.model.property.payments.Currency;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.property.cars.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-car")
public class ManageCarsController {
    private final CarService carService;
    private final CarBaseService carBaseService;
    private final DepartmentService departmentService;
    private final RentService rentService;
    private final RetrieveService retrieveService;
    private final EmployeeService employeeService;
    private final CurrencyService currencyService;
    private final CountryService countryService;
    private final Utility u;

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchCarsPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> departments = employeeService.getDepartmentsByUserContext(cud);
            List<Car> cars = carService.findByDepartments(departments);

            map.addAttribute("results", map.getOrDefault("results", cars));

            Map<String, Object> carProperties = carService.getFilterProperties(cars);
            map.addAttribute("brands", carProperties.get("brands"));
            map.addAttribute("types", carProperties.get("types"));
            map.addAttribute("seats", carProperties.get("seats"));

            map.addAttribute("countries", countryService.findAll());
            map.addAttribute("departments", departments);
            map.addAttribute("statuses", Car.CarStatus.values());
            map.addAttribute("currency", departments.get(0).getCountry().getCurrency());

            map.addAttribute("patterns", carBaseService.findAllSorted());

            map.addAttribute("searchCarsForm", map.getOrDefault("searchCarsForm", new SearchCarsForm()));
            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterCarForm()));

            return "management/searchCars";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{carId}")
    public String viewCarPage(ModelMap map, @PathVariable(value = "carId") Long carId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Car car = carService.findCarById(carId);
            if (employeeService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-car";
            }

            Department department = car.getDepartment();
            Currency currency = department.getCountry().getCurrency();

            Double exchange = currency.getExchange();
            Double multiplier = exchange * department.getMultiplier();

            map.addAttribute("currency", currency.getCode());
            map.addAttribute("price_day", u.roundCurrency(car.getCarBase().getPriceDay() * multiplier));
            map.addAttribute("price_deposit", u.roundCurrency(car.getCarBase().getDepositValue() * exchange));


            map.addAttribute("mileage_form", map.getOrDefault("mileage_form", new ChangeCarMileageForm(car.getMileage())));

            map.addAttribute("department_form", map.getOrDefault("department_form", new ChangeCarDepartmentForm(department.getId())));
            map.addAttribute("departments", departmentService.findAll());

            map.addAttribute("status_form", map.getOrDefault("status_form", new ChangeCarStatusForm(car.getCarStatus().name())));
            map.addAttribute("statuses", Car.CarStatus.values());


            Rent currentRent = rentService.findActiveByCar(car).orElse(null);
            int maxArchived = 50;
            List<Retrieve> archivedRentals = retrieveService.findRetrievalsByCar(car, maxArchived);
            map.addAttribute("currentRental", currentRent);
            map.addAttribute("archivedRentals", archivedRentals);
            map.addAttribute("maxArchived", maxArchived);

            if (currentRent == null && archivedRentals.isEmpty()) {
                map.addAttribute("confirmation_form", new ConfirmationForm());
            }

            //Hardcoded map coordinates due to lack of real data
            map.addAttribute("latitude", "50.556140369367725");
            map.addAttribute("longitude", "18.888311942515067");

            map.addAttribute("car", car);
            return "management/viewCar";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-car";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-car";
        }
    }

    //Search cars page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/search")
    public String filterCarsButton(@ModelAttribute("searchCarsForm") @Valid SearchCarsForm form, Errors err, RedirectAttributes redAtt) {
        redAtt.addFlashAttribute("searchCarsForm", form);
        if (err.hasErrors()) {
            return "redirect:/mg-car";
        }
        try {
            redAtt.addFlashAttribute("results", carService.findByCriteria(form));
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/register")
    public String registerCarButton(@ModelAttribute("register_form") @Valid RegisterCarForm form, Errors err, RedirectAttributes redAtt) {
        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
            redAtt.addFlashAttribute("register_form", form);
            return "redirect:/mg-car";
        }

        HttpStatus status = carService.register(form);
        if (status.equals(HttpStatus.CREATED)) {
            redAtt.addFlashAttribute(MSG_KEY, "Success: Car successfully registered.");
        } else {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car";
    }

    //View car page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/{id}/status")
    public String statusChangeButton(@ModelAttribute("status_form") @Valid ChangeCarStatusForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
        redAtt.addAttribute("carId", carId);

        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Car car = carService.findCarById(carId);
            if (employeeService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-car";
            }

            if (errors.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                redAtt.addFlashAttribute("status_form", form);
                return "redirect:/mg-car/{carId}";
            }

            Car.CarStatus carStatus = Car.CarStatus.valueOf(form.getStatus());
            HttpStatus status = carService.handleStatus(car, carStatus);
            if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Car status successfully changed to - " + carStatus.name().substring(7));
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Rejected due to active reservation.");
            }
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car/{carId}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/mileage")
    public String mileageChangeButton(@ModelAttribute("mileage_form") @Valid ChangeCarMileageForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
        redAtt.addAttribute("carId", carId);

        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Car car = carService.findCarById(carId);
            if (employeeService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-car";
            }

            if (errors.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                redAtt.addFlashAttribute("mileage_form", form);
                return "redirect:/mg-car/{carId}";
            }

            HttpStatus status = carService.updateCarMileage(car, form.getMileage());
            if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Car mileage successfully changed to - " + form.getMileage());
            }
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car/{carId}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/department")
    public String departmentChangeButton(@ModelAttribute("department_form") @Valid ChangeCarDepartmentForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
        redAtt.addAttribute("carId", carId);

        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Car car = carService.findCarById(carId);
            if (employeeService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-car";
            }

            if (errors.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                redAtt.addFlashAttribute("department_form", form);
                return "redirect:/mg-car/{carId}";
            }

            Department department = departmentService.findById(form.getDepartmentId());
            HttpStatus status = carService.updateCarLocation(car, department);
            if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Car location successfully changed to - " + department.getCity() + ", " + department.getStreet() + " " + department.getBuilding());
            }
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car/{carId}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
    public String deleteCarButton(@ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Car car = carService.findCarById(carId);
            if (employeeService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-car";
            }

            if (errors.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/{carId}";
            }

            HttpStatus status = carService.handleCarDelete(car.getId());
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Car \"" + car.getPlate() + "\" successfully deleted");
                return "redirect:/mg-car";
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Car cannot have rental history");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-car/{carId}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }

        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/logs")
    public String checkLogsButton(RedirectAttributes redAtt, @PathVariable("id") Long carId, @RequestParam("o_id") Long operationId) {
        redAtt.addFlashAttribute("previousPage", "/mg-car/" + carId);
        return "redirect:/archive/" + operationId;
    }
}
