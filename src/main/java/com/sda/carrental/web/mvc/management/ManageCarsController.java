package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.*;
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
    private final UserService userService;
    private final RentService rentService;
    private final RetrieveService retrieveService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchCarsPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> departments = departmentService.getDepartmentsByUserContext(cud);
            List<Car> cars = carService.findByDepartments(departments);

            map.addAttribute("results", map.getOrDefault("results", cars));

            Map<String, Object> carProperties = carService.getFilterProperties(cars);
            map.addAttribute("brands", carProperties.get("brands"));
            map.addAttribute("types", carProperties.get("types"));
            map.addAttribute("seats", carProperties.get("seats"));

            map.addAttribute("countries", Country.values());
            map.addAttribute("departments", departments);
            map.addAttribute("statuses", Car.CarStatus.values());

            map.addAttribute("patterns", carBaseService.findAll());

            map.addAttribute("searchCarsForm", map.getOrDefault("searchCarsForm", new SearchCarsForm()));
            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterCarForm()));

            return "management/searchCars";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected exception");
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value="/car-bases")
    public String searchCarBasesPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            List<CarBase> carBases = carBaseService.findAll();

            map.addAttribute("results", map.getOrDefault("results", carBases));

            Map<String, Object> carProperties = carBaseService.getFilterProperties(carBases, true);
            map.addAttribute("brands", carProperties.get("brands"));
            map.addAttribute("types", carProperties.get("types"));
            map.addAttribute("seats", carProperties.get("seats"));
            map.addAttribute("years", carProperties.get("years"));

            map.addAttribute("all_car_types", CarBase.CarType.values());

            map.addAttribute("searchCarBasesForm", map.getOrDefault("searchCarBasesForm", new SearchCarBasesFilterForm()));
            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterCarBaseForm()));

            return "management/searchCarBases";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected exception");
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{carId}")
    public String viewCarPage(ModelMap map, @PathVariable(value = "carId") Long carId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Car car = carService.findCarById(carId);
            if (userService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
                return "redirect:/mg-car";
            }

            map.addAttribute("mileage_form", map.getOrDefault("mileage_form", new ChangeCarMileage(car.getMileage())));

            map.addAttribute("department_form", map.getOrDefault("department_form", new ChangeCarDepartment(car.getDepartment().getId())));
            map.addAttribute("departments", departmentService.findAll());

            map.addAttribute("status_form", map.getOrDefault("status_form", new ChangeCarStatus(car.getCarStatus().name())));
            map.addAttribute("statuses", Car.CarStatus.values());

            map.addAttribute("currentRental", rentService.findActiveByCar(car).orElse(null));
            map.addAttribute("previousRentals", retrieveService.findRetrievalsByCar(car, 3));

            //Hardcoded map coordinates due to lack of real data
            map.addAttribute("latitude", "50.556140369367725");
            map.addAttribute("longitude", "18.888311942515067");

            map.addAttribute("car", car);
            return "management/viewCar";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
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

        redAtt.addFlashAttribute("results", carService.findByCriteria(form));
        return "redirect:/mg-car";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/register")
    public String registerCarButton(@ModelAttribute("register_form") @Valid RegisterCarForm form, Errors err, RedirectAttributes redAtt) {
        if (err.hasErrors()) {
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            redAtt.addFlashAttribute("register_form", form);
            return "redirect:/mg-car";
        }

        HttpStatus status = carService.register(form);
        if (status.equals(HttpStatus.CREATED)) {
            redAtt.addFlashAttribute("message", "Success: Car successfully registered.");
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "Failure: Provided car information might not be valid.");
        } else {
            redAtt.addFlashAttribute("message", "Failure: Unexpected database error.");
        }
        return "redirect:/mg-car";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/select")
    public String carViewButton(RedirectAttributes redAtt, @RequestParam("select_button") Long carId) {
        redAtt.addAttribute("carId", carId);
        return "redirect:/mg-car/{carId}";
    }

    //Search car bases page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/car-bases/search")
    public String filterCarBasesButton(@ModelAttribute("searchCarBasesForm") @Valid SearchCarBasesFilterForm form, Errors err, RedirectAttributes redAtt) {
        redAtt.addFlashAttribute("searchCarBasesForm", form);
        if (err.hasErrors()) {
            return "redirect:/mg-car/car-bases";
        }

        redAtt.addFlashAttribute("results", carBaseService.findCarBasesByForm(form));
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/car-bases/register")
    public String registerCarBaseButton(@ModelAttribute("register_form") @Valid RegisterCarBaseForm form, Errors err, RedirectAttributes redAtt) {
        if (err.hasErrors()) {
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            redAtt.addFlashAttribute("register_form", form);
            return "redirect:/mg-car/car-bases";
        }

        HttpStatus status = carBaseService.register(form);
        if (status.equals(HttpStatus.CREATED)) {
            redAtt.addFlashAttribute("message", "Success: Pattern successfully registered.");
        } else {
            redAtt.addFlashAttribute("message", "Failure: Unexpected database error.");
        }
        return "redirect:/mg-car/car-bases";
    }

    //View car page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/{id}/status")
    public String statusChangeButton(@ModelAttribute("status_form") @Valid ChangeCarStatus form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
        redAtt.addAttribute("carId", carId);

        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Car car = carService.findCarById(carId);
            if (userService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
                return "redirect:/mg-car";
            }

            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
                redAtt.addFlashAttribute("status_form", form);
                return "redirect:/mg-car/{carId}";
            }

            Car.CarStatus carStatus = Car.CarStatus.valueOf(form.getStatus());
            HttpStatus status = carService.updateCarStatus(car, carStatus);
            if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute("message", "Success: Car status successfully changed to - " + carStatus.name().substring(7));
            }
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }
        return "redirect:/mg-car/{carId}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/mileage")
    public String mileageChangeButton(@ModelAttribute("mileage_form") @Valid ChangeCarMileage form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
        redAtt.addAttribute("carId", carId);

        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Car car = carService.findCarById(carId);
            if (userService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
                return "redirect:/mg-car";
            }

            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
                redAtt.addFlashAttribute("mileage_form", form);
                return "redirect:/mg-car/{carId}";
            }

            HttpStatus status = carService.updateCarMileage(car, form.getMileage());
            if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute("message", "Success: Car mileage successfully changed to - " + form.getMileage());
            }
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }
        return "redirect:/mg-car/{carId}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/department")
    public String departmentChangeButton(@ModelAttribute("department_form") @Valid ChangeCarDepartment form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
        redAtt.addAttribute("carId", carId);

        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Car car = carService.findCarById(carId);
            if (userService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
                return "redirect:/mg-car";
            }

            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
                redAtt.addFlashAttribute("department_form", form);
                return "redirect:/mg-car/{carId}";
            }

            Department department = departmentService.findDepartmentWhereId(form.getDepartmentId());
            HttpStatus status = carService.updateCarLocation(car, department);
            if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute("message", "Success: Car location successfully changed to - " + department.getCity() + ", " + department.getAddress());
            }
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }
        return "redirect:/mg-car/{carId}";
    }
}
