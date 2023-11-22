package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.Utility;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-car/car-bases")
public class ManageCarBasesController {
    private final Utility utility;
    private final CarService carService;
    private final CarBaseService carBaseService;
    private final DepartmentService departmentService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchCarBasesPage(ModelMap map, HttpServletRequest res, RedirectAttributes redAtt) {
        try {
            List<CarBase> carBases = carBaseService.findAllSorted();
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

    @RequestMapping(method = RequestMethod.GET, value = "/{carBaseId}")
    public String viewCarBasePage(ModelMap map, @PathVariable(value = "carBaseId") Long carBaseId, HttpServletRequest req, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> departments = departmentService.getDepartmentsByUserContext(cud);
            map.addAttribute("departments", departments);

            utility.retrieveSessionMessage(map, req);

            CarBase carBase = carBaseService.findById(carBaseId);

            map.addAttribute("confirmation_form", new ConfirmationForm());
            map.addAttribute("update_image_form", new UpdateCarBaseImageForm());
            map.addAttribute("split_form", map.getOrDefault("split_form", new SplitCarBaseForm()));
            map.addAttribute("update_price_form", map.getOrDefault("update_price_form", new UpdateCarBasePricesForm(carBase.getPriceDay(), carBase.getDepositValue())));
            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterCarForm(carBase.getId())));


            map.addAttribute("result", carBase);
            map.addAttribute("carList", carService.findCarsByDepartmentsAndCarBase(departments, carBase));
            map.addAttribute("patterns", carBaseService.findAllSorted());
            map.addAttribute("resultSize", carService.getCarSizeByCarBase(carBase.getId()));

            return "management/viewCarBase";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
            return "redirect:/mg-car/car-bases";
        }
    }

    //Search car bases page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/search")
    public String filterCarBasesButton(@ModelAttribute("searchCarBasesForm") @Valid SearchCarBasesFilterForm form, Errors err, RedirectAttributes redAtt) {
        redAtt.addFlashAttribute("searchCarBasesForm", form);
        if (err.hasErrors()) {
            return "redirect:/mg-car/car-bases";
        }

        redAtt.addFlashAttribute("results", carBaseService.findCarBasesByForm(form));
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/register")
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

    //View car base page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/split")
    public String splitCarBaseButton(@ModelAttribute("split_form") @Valid SplitCarBaseForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("split_form", form);
                redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            CarBase tcb = carBaseService.findById(form.getPattern());
            if (cb.getId().equals(tcb.getId())) throw new IllegalActionException();

            HttpStatus status = carService.splitCarsToCarBase(cb, tcb, form.getCars());
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute("message", "Success: Split " + form.getCars().size() + " cars to '" + tcb.getBrand() + " " + tcb.getModel() + " [" + tcb.getYear() + "/" + tcb.getSeats() + "/pd: " +tcb.getPriceDay() + "/d: " +tcb.getDepositValue() + "]' pattern");
                return "redirect:/mg-car/car-bases/{carBaseId}";
            } else if (status.equals(HttpStatus.BAD_REQUEST ) || status.equals(HttpStatus.CONFLICT)) {
                redAtt.addFlashAttribute("message", "Failure: Provided values are invalid");
            } else {
                redAtt.addFlashAttribute("message", "Failure: Unexpected error");
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failure: Not found");
        } catch (IllegalActionException err) {
            redAtt.addFlashAttribute("message", "Failure: Target pattern is equal to current pattern");
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "Failure: Unexpected value");
        }
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/delete")
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

    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/update-image")
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

    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/update-price")
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

    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/register-car")
    public String registerCarButton(@ModelAttribute("register_form") @Valid RegisterCarForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
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
