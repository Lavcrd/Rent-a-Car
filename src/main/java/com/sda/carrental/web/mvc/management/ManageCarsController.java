package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.Utility;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Rent;
import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.car.CarBase;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-car")
public class ManageCarsController {
    private final Utility utility;
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

    @RequestMapping(method = RequestMethod.GET, value = "/car-bases")
    public String searchCarBasesPage(ModelMap map, HttpServletRequest res, RedirectAttributes redAtt) {
        try {
            List<CarBase> carBases = carBaseService.findAll();
            utility.retrieveSessionMessage(map, res);

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

            map.addAttribute("mileage_form", map.getOrDefault("mileage_form", new ChangeCarMileageForm(car.getMileage())));

            map.addAttribute("department_form", map.getOrDefault("department_form", new ChangeCarDepartmentForm(car.getDepartment().getId())));
            map.addAttribute("departments", departmentService.findAll());

            map.addAttribute("status_form", map.getOrDefault("status_form", new ChangeCarStatusForm(car.getCarStatus().name())));
            map.addAttribute("statuses", Car.CarStatus.values());


            Rent currentRent = rentService.findActiveByCar(car).orElse(null);
            List<Retrieve> previousRentals = retrieveService.findRetrievalsByCar(car, 3);
            map.addAttribute("currentRental", currentRent);
            map.addAttribute("previousRentals", previousRentals);

            if (currentRent == null && previousRentals.isEmpty()) {
                map.addAttribute("confirmation_form", new ConfirmationForm());
            }

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

    @RequestMapping(method = RequestMethod.GET, value = "/car-bases/{carBaseId}")
    public String viewCarBasePage(ModelMap map, @PathVariable(value = "carBaseId") Long carBaseId, HttpServletRequest req, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> departments = departmentService.getDepartmentsByUserContext(cud);
            map.addAttribute("departments", departments);

            utility.retrieveSessionMessage(map, req);

            CarBase carBase = carBaseService.findById(carBaseId);

            map.addAttribute("confirmation_form", new ConfirmationForm());
            map.addAttribute("update_image_form", new UpdateCarBaseImageForm());
            map.addAttribute("update_price_form", map.getOrDefault("update_price_form", new UpdateCarBasePricesForm(carBase.getPriceDay(), carBase.getDepositValue())));
            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterCarForm(carBase.getId())));


            map.addAttribute("result", carBase);
            map.addAttribute("resultSize", carService.getCarSizeByCarBase(carBase.getId()));

            return "management/viewCarBase";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
            return "redirect:/mg-car/car-bases";
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
    public String statusChangeButton(@ModelAttribute("status_form") @Valid ChangeCarStatusForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
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
            HttpStatus status = carService.handleStatus(car, carStatus);
            if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute("message", "Success: Car status successfully changed to - " + carStatus.name().substring(7));
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute("message", "Failure: Rejected due to active reservation.");
            }
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }
        return "redirect:/mg-car/{carId}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/mileage")
    public String mileageChangeButton(@ModelAttribute("mileage_form") @Valid ChangeCarMileageForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
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
    public String departmentChangeButton(@ModelAttribute("department_form") @Valid ChangeCarDepartmentForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
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

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
    public String deleteCarButton(@ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("id") Long carId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Car car = carService.findCarById(carId);
            if (userService.hasNoAccessToProperty(cud, car)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
                return "redirect:/mg-car";
            }

            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/{carId}";
            }

            HttpStatus status = carService.handleCarDelete(car.getId());
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute("message", "Success: Car \"" + car.getPlate() + "\" successfully deleted");
                return "redirect:/mg-car";
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute("message", "Failure: Car cannot have rental history");
            } else {
                redAtt.addFlashAttribute("message", "Failure: Unexpected error");
            }
            return "redirect:/mg-car/{carId}";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }

        return null;
    }

    //View car base page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/car-bases/{carBaseId}/delete")
    public String deleteCarBaseButton(@ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            HttpStatus status = carBaseService.handleCarBaseDelete(cb);
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute("message", "Success: Pattern '" + cb.getBrand() + ' ' + cb.getModel() + "' successfully deleted");
                return "redirect:/mg-car/car-bases";
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute("message", "Failure: Car base cannot be deleted while it has cars");
            } else {
                redAtt.addFlashAttribute("message", "Failure: Unexpected error");
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/car-bases/{carBaseId}/update-image")
    public String updateCarBaseImageButton(@ModelAttribute("update_image_form") @Valid UpdateCarBaseImageForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            HttpStatus status = carBaseService.handleCarBaseImageUpdate(cb, form.getImage());
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute("message", "Success: Image file of '" + cb.getBrand() + ' ' + cb.getModel() + "' successfully updated");
                return "redirect:/mg-car/car-bases/{carBaseId}";
            } else {
                redAtt.addFlashAttribute("message", "Failure: Unexpected error");
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/car-bases/{carBaseId}/update-price")
    public String updateCarBasePricesButton(@ModelAttribute("update_price_form") @Valid UpdateCarBasePricesForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("update_price_form", form);
                redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            HttpStatus status = carBaseService.handleCarBasePricesUpdate(cb, form);
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute("message", "Success: Prices of '" + cb.getBrand() + ' ' + cb.getModel() + "' successfully updated");
                return "redirect:/mg-car/car-bases/{carBaseId}";
            } else {
                redAtt.addFlashAttribute("message", "Failure: Unexpected error");
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/car-bases/{carBaseId}/register-car")
    public String updateCarBasePricesButton(@ModelAttribute("register_form") @Valid RegisterCarForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
                redAtt.addFlashAttribute("register_form", form);
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            if (!cb.getId().equals(form.getPattern())) throw new IllegalActionException();

            HttpStatus status = carService.register(form);
            if (status.equals(HttpStatus.CREATED)) {
                redAtt.addFlashAttribute("message", "Success: Car " + form.getPlate() + " successfully registered.");
            } else if (status.equals(HttpStatus.NOT_FOUND)) {
                redAtt.addFlashAttribute("message", "Failure: Provided car information might not be valid.");
            } else {
                redAtt.addFlashAttribute("message", "Failure: Unexpected database error.");
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }
        return "redirect:/mg-car/car-bases";
    }
}
